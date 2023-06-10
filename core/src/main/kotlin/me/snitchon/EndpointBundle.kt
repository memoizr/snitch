package me.snitchon

import me.snitchon.request.RequestWrapper
import me.snitchon.response.HttpResponse
import me.snitchon.service.Endpoint

data class EndpointBundle<T : Any>(
    val endpoint: Endpoint<T>,
    val response: EndpointResponse,
    val handler: (RequestWrapper, ResponseWrapper) -> HttpResponse<*, *>
)