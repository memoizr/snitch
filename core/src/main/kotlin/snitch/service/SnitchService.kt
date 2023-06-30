package snitch.service

import snitch.parsing.Parser
import snitch.request.RequestWrapper
import snitch.response.HttpResponse
import snitch.router.Routes
import snitch.types.EndpointBundle
import kotlin.reflect.KClass

interface SnitchService {
    val config: snitch.config.SnitchConfig
    fun registerMethod(endpointBundle: EndpointBundle<*>, path: String)
    fun onRoutes(routerConfiguration: Routes): RoutedService
    fun onStop(action: () -> Unit): SnitchService

    fun <T : Throwable, R : HttpResponse<*, *>> handleException(
        exceptionClass: KClass<T>,
        exceptionHandler: context(Parser) RequestWrapper.(T) -> R
    )
}


