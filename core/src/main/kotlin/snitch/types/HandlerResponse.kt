package snitch.types

import snitch.request.TypedRequestWrapper
import snitch.response.HttpResponse
import kotlin.reflect.KType

data class HandlerResponse<Request : Any, Response, S : StatusCodes>(
    val statusCodes: KType,
    val type: KType,
    val handler:
    TypedRequestWrapper<Request>.() -> HttpResponse<Response, S>
)