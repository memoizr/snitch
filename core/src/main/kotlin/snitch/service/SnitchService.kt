package snitch.service

import snitch.request.RequestWrapper
import snitch.response.HttpResponse
import snitch.router.Routes
import snitch.types.EndpointBundle
import snitch.types.Parser
import kotlin.reflect.KClass

interface SnitchService {
    val config: snitch.config.SnitchConfig
    val parser: Parser
    fun registerMethod(endpointBundle: EndpointBundle<*>, path: String)
    fun onRoutes(routerConfiguration: Routes): RoutedService
    fun onStop(action: () -> Unit): SnitchService

    fun <T : Throwable, R : HttpResponse<*, *>> handleException(
        exceptionClass: KClass<T>,
        exceptionHandler: RequestWrapper.(T) -> R
    )
}


