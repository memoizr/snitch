package snitch.types

import snitch.request.RequestWrapper
import snitch.response.HttpResponse
import snitch.service.Endpoint

data class EndpointBundle<T : Any>(
    val endpoint: Endpoint<T>,
    val response: EndpointResponse,
    val handlerResponse: HandlerResponse<Any, Any, out StatusCodes>,
    val handler: (RequestWrapper) -> HttpResponse<*, *>
) {
    val params = (endpoint.headerParams + endpoint.queryParams + endpoint.pathParams)
}