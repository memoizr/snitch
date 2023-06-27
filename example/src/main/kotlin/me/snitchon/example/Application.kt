package me.snitchon.example

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import io.jsonwebtoken.JwtException
import me.snitchon.config.SnitchConfig
import me.snitchon.config.SnitchConfig.Service
import me.snitchon.documentation.generateDocumentation
import me.snitchon.documentation.servePublicDocumenation
import me.snitchon.example.ApplicationModule.logger
import me.snitchon.example.database.DBModule.postgresDatabase
import me.snitchon.example.types.ForbiddenException
import me.snitchon.example.types.ValidationException
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.service.RoutedService
import me.snitchon.service.exceptionhandling.handleInvalidParameters
import me.snitchon.service.exceptionhandling.handleParsingException
import me.snitchon.service.exceptionhandling.handleUnregisteredParameters
import me.snitchon.types.ErrorResponse
import net.logstash.logback.encoder.LogstashEncoder
import org.slf4j.LoggerFactory
import undertow.snitch.spark.snitch

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

        setUpDatabase()

        return snitch(GsonJsonParser, SnitchConfig(Service(port = port)))
            .onRoutes(rootRouter)
            .handleExceptions()
    }

    private fun setUpDatabase() {
        postgresDatabase().addMissingColumns()
    }
}

fun main() {
    postgresDatabase().createSchema()
    postgresDatabase().addMissingColumns()
    Application.setup(3000)
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
