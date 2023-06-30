package snitch.types

import snitch.parsing.Parser
import snitch.response.HttpResponse
import snitch.request.TypedRequestWrapper
import kotlin.reflect.KType

data class HandlerResponse<Request : Any, Response, S : StatusCodes>(
    val statusCodes: KType,
    val type: KType,
    val handler:
    context(Parser) TypedRequestWrapper<Request>.() -> HttpResponse<Response, S>
)