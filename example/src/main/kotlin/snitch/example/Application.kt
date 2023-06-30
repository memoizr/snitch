package snitch.example

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import io.jsonwebtoken.JwtException
import snitch.config.SnitchConfig
import snitch.config.SnitchConfig.Service
import snitch.documentation.generateDocumentation
import snitch.documentation.servePublicDocumenation
import snitch.example.ApplicationModule.logger
import snitch.parsers.GsonJsonParser
import snitch.service.RoutedService
import snitch.service.exceptionhandling.handleInvalidParameters
import snitch.service.exceptionhandling.handleParsingException
import snitch.service.exceptionhandling.handleUnregisteredParameters
import snitch.types.ErrorResponse
import net.logstash.logback.encoder.LogstashEncoder
import org.slf4j.LoggerFactory
import snitch.example.database.DBModule.postgresDatabase
import snitch.example.types.ForbiddenException
import snitch.example.types.ValidationException
import snitch.undertow.snitch

object Application {
    init {
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

        val logstashEncoder = LogstashEncoder()
        logstashEncoder.context = loggerContext
        logstashEncoder.fieldNames.timestamp = "time"
        logstashEncoder.fieldNames.version = "version"
        logstashEncoder.start()

        val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
        rootLogger.level = Level.INFO
    }

    fun setup(port: Int): RoutedService {

        snitch.example.Application.setUpDatabase()

        return snitch(GsonJsonParser, SnitchConfig(Service(port = port)))
            .onRoutes(snitch.example.rootRouter)
            .handleExceptions()
    }

    private fun setUpDatabase() {
        postgresDatabase().addMissingColumns()
    }
}

fun main() {
    postgresDatabase().createSchema()
    postgresDatabase().addMissingColumns()
    snitch.example.Application.setup(3000)
        .start()
        .generateDocumentation()
        .servePublicDocumenation()
}

fun RoutedService.handleExceptions(): RoutedService =
    handleInvalidParameters()
        .handleParsingException()
        .handleUnregisteredParameters()
        .handleException(ValidationException::class) {
            ErrorResponse(400, it.reason)
                .also { logger().error(it.toString()) }
                .badRequest()
        }
        .handleException(JwtException::class) {
            ErrorResponse(401, "unauthorized")
                .also { logger().error(it.toString()) }
                .unauthorized()
        }
        .handleException(ForbiddenException::class) {
            ErrorResponse(403, "forbidden")
                .also { logger().error(it.toString()) }
                .forbidden()
        }
        .handleException(Throwable::class) {
            ErrorResponse(500, "something went wrong").serverError()
        }
