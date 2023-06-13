package me.snitchon.example

import undertow.snitch.spark.UndertowSnitchService
import me.snitchon.config.SnitchConfig
import me.snitchon.config.SnitchConfig.Service
import me.snitchon.example.DBModule.db
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.service.RoutedService
import me.snitchon.service.exceptionhandling.handleInvalidParameters
import me.snitchon.service.exceptionhandling.handleParsingException
import me.snitchon.service.exceptionhandling.handleUnregisteredParameters
import me.snitchon.types.ErrorResponse
import org.jetbrains.exposed.sql.*
import java.util.*

object Application {
    fun start(port: Int): RoutedService {
        db().addMissingColumns()

        return UndertowSnitchService(GsonJsonParser, SnitchConfig(Service(port = port)))
            .setRoutes(router)
            .handleExceptions()
    }
}

fun RoutedService.handleExceptions(): RoutedService =
    handleInvalidParameters()
        .handleParsingException()
        .handleUnregisteredParameters()
        .handleException(ValidationException::class) {
            ErrorResponse(400, it.reason).badRequest
        }
        .handleException(Throwable::class) {
            "somethingwentwrong".serverError
        }

fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    return emailRegex.matches(this)
}