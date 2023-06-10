package me.snitchon.service.exceptionhandling

import me.snitchon.parameters.HeaderParameter
import me.snitchon.parameters.InvalidParametersException
import me.snitchon.parameters.PathParam
import me.snitchon.parameters.QueryParameter
import me.snitchon.parsing.ParsingException
import me.snitchon.request.UnregisteredParamException
import me.snitchon.response.ErrorHttpResponse
import me.snitchon.response.badRequest
import me.snitchon.response.serverError
import me.snitchon.service.RoutedService
import me.snitchon.types.ErrorResponse

fun RoutedService.handleInvalidParameters() =
    handleException<InvalidParametersException, _> { ex, wrap ->
        ErrorResponse(400, ex.reasons).badRequest
    }

fun RoutedService.handleUnregisteredParameters() =
    handleException<UnregisteredParamException, _> { ex, wrap ->
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
    handleException<ParsingException, _> { ex, req ->
        ErrorResponse(400, "Invalid body parameter").badRequest
    }
