package me.snitch.service

import me.snitch.config.SnitchConfig
import me.snitch.parsing.Parser
import me.snitch.request.RequestWrapper
import me.snitch.response.HttpResponse
import me.snitch.router.Routes
import me.snitch.types.EndpointBundle
import kotlin.reflect.KClass

interface SnitchService {
    val config: SnitchConfig
    fun registerMethod(endpointBundle: EndpointBundle<*>, path: String)
    fun onRoutes(routerConfiguration: Routes): RoutedService
    fun onStop(action: () -> Unit): SnitchService

    fun <T : Throwable, R : HttpResponse<*, *>> handleException(
        exceptionClass: KClass<T>,
        exceptionHandler: context(Parser) RequestWrapper.(T) -> R
    )
}


