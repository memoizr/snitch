package me.snitchon.types

import me.snitchon.parsing.Parser
import me.snitchon.request.Context
import me.snitchon.response.HttpResponse
import kotlin.reflect.KType

data class HandlerResponse<Request : Any, Response, S : StatusCodes>(
    val statusCodes: KType,
    val type: KType,
    val handler:
    context(Parser) Context<Request>.() -> HttpResponse<Response, S>
)