package me.snitchon.example

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.FileAppender
import io.jsonwebtoken.JwtException
import undertow.snitch.spark.UndertowSnitchService
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

        return UndertowSnitchService(GsonJsonParser, SnitchConfig(Service(port = port)))
            .setRoutes(rootRouter)
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
            ErrorResponse(400, it.reason).badRequest()
        }
        .handleException(JwtException::class) {
            ErrorResponse(401, "unauthorized").unauthorized()
        }
        .handleException(ForbiddenException::class) {
            ErrorResponse(403, "forbidden").forbidden()
        }
        .handleException(Throwable::class) {
            ErrorResponse(500, "something went wrong").serverError()
        }
