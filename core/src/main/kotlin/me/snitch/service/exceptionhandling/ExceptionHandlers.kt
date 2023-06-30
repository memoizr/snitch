package me.snitch.service.exceptionhandling

import me.snitch.parameters.HeaderParameter
import me.snitch.parameters.InvalidParametersException
import me.snitch.parameters.PathParam
import me.snitch.parameters.QueryParameter
import me.snitch.parsing.ParsingException
import me.snitch.validation.UnregisteredParamException
import me.snitch.service.RoutedService
import me.snitch.types.ErrorResponse

fun RoutedService.handleInvalidParameters() =
    handleException<InvalidParametersException, _> { ex ->
        ErrorResponse(400, ex.reasons).badRequest()
    }

fun RoutedService.handleUnregisteredParameters() =
    handleException<UnregisteredParamException, _> { ex ->
        val type = when (ex.param) {
            is HeaderParameter -> "header"
            is QueryParameter -> "query"
            is PathParam -> "path"
        }

        ErrorResponse(
            500,
            "Attempting to use unregistered $type parameter `${ex.param.name}`"
        ).serverError()
    }

fun RoutedService.handleParsingException() =
    handleException<ParsingException, _> { ex ->
        ErrorResponse(400, "Invalid body parameter").badRequest()
    }
