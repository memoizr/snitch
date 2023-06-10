package me.snitchon.service

import me.snitchon.Router
import me.snitchon.config.SnitchConfig
import me.snitchon.parsing.Parser
import me.snitchon.request.RequestWrapper
import me.snitchon.response.HttpResponse
import me.snitchon.types.EndpointBundle
import kotlin.reflect.KClass

interface SnitchService {
    val config: SnitchConfig
    fun registerMethod(endpointBundle: EndpointBundle<*>, path: String)
    fun setRoutes(routerConfiguration: Router.() -> Unit): RoutedService
    fun start()
    fun stop()

    fun <T : Exception, R : HttpResponse<*, *>> handleException(
        exceptionClass: KClass<T>,
        exceptionHandler: context(Parser) (T, RequestWrapper) -> R
    )
}


