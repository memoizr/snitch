package me.snitchon.service.exceptionhandling

import me.snitchon.parameters.HeaderParameter
import me.snitchon.parameters.InvalidParametersException
import me.snitchon.parameters.PathParam
import me.snitchon.parameters.QueryParameter
import me.snitchon.parsing.ParsingException
import me.snitchon.validation.UnregisteredParamException
import me.snitchon.service.RoutedService
import me.snitchon.types.ErrorResponse

fun RoutedService.handleInvalidParameters() =
    handleException<InvalidParametersException, _> { ex ->
        ErrorResponse(400, ex.reasons).badRequest
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
        ).serverError
    }

fun RoutedService.handleParsingException() =
    handleException<ParsingException, _> { ex ->
        ErrorResponse(400, "Invalid body parameter").badRequest
    }
