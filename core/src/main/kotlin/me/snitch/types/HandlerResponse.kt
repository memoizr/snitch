package me.snitch.types

import me.snitch.parsing.Parser
import me.snitch.request.TypedRequestWrapper
import me.snitch.response.HttpResponse
import kotlin.reflect.KType

data class HandlerResponse<Request : Any, Response, S : StatusCodes>(
    val statusCodes: KType,
    val type: KType,
    val handler:
    context(Parser) TypedRequestWrapper<Request>.() -> HttpResponse<Response, S>
)