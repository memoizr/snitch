package me.snitchon.service

import me.snitchon.router.Router
import me.snitchon.config.SnitchConfig
import me.snitchon.parsing.Parser
import me.snitchon.request.ImplementationRequestWrapper
import me.snitchon.response.HttpResponse
import me.snitchon.types.EndpointBundle
import kotlin.reflect.KClass

interface SnitchService {
    val config: SnitchConfig
    fun registerMethod(endpointBundle: EndpointBundle<*>, path: String)
    fun setRoutes(routerConfiguration: Router.() -> Unit): RoutedService
    fun onStop(action: () -> Unit): SnitchService

    fun <T : Throwable, R : HttpResponse<*, *>> handleException(
        exceptionClass: KClass<T>,
        exceptionHandler: context(Parser) ImplementationRequestWrapper.(T) -> R
    )
}


