package snitch.service.exceptionhandling

import snitch.parameters.HeaderParameter
import snitch.parameters.PathParam
import snitch.parameters.QueryParameter
import snitch.parsers.ParsingException
import snitch.types.ErrorResponse
import snitch.parameters.InvalidParametersException
import snitch.service.RoutedService
import snitch.validation.UnregisteredParamException

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
