package me.snitch.types

import me.snitch.request.RequestWrapper
import me.snitch.response.HttpResponse
import me.snitch.service.Endpoint

data class EndpointBundle<T : Any>(
    val endpoint: Endpoint<T>,
    val response: EndpointResponse,
    val handlerResponse: HandlerResponse<Any, Any, out StatusCodes>,
    inline val handler: (RequestWrapper) -> HttpResponse<*, *>
) {
    val params = (endpoint.headerParams + endpoint.queryParams + endpoint.pathParams)
}