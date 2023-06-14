package me.snitchon.example

import io.jsonwebtoken.MalformedJwtException
import undertow.snitch.spark.UndertowSnitchService
import me.snitchon.config.SnitchConfig
import me.snitchon.config.SnitchConfig.Service
import me.snitchon.example.database.DBModule.postgresDatabase
import me.snitchon.example.types.ValidationException
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.service.RoutedService
import me.snitchon.service.exceptionhandling.handleInvalidParameters
import me.snitchon.service.exceptionhandling.handleParsingException
import me.snitchon.service.exceptionhandling.handleUnregisteredParameters
import me.snitchon.types.ErrorResponse

object Application {
    fun start(port: Int): RoutedService {
        setUpDatabase()

        return UndertowSnitchService(GsonJsonParser, SnitchConfig(Service(port = port)))
            .setRoutes(router)
            .handleExceptions()
    }

    private fun setUpDatabase() {
        postgresDatabase().addMissingColumns()
    }
}

fun RoutedService.handleExceptions(): RoutedService =
    handleInvalidParameters()
        .handleParsingException()
        .handleUnregisteredParameters()
        .handleException(MalformedJwtException::class) {
            it.printStackTrace()
            ErrorResponse(401, "unauthorized").unauthorized
        }
        .handleException(ValidationException::class) {
            it.printStackTrace()
            ErrorResponse(400, it.reason).badRequest
        }
        .handleException(Throwable::class) {
            it.printStackTrace()
            ErrorResponse(500, "something went wrong").serverError
        }
