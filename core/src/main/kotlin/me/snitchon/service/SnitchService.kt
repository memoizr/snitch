package me.snitchon.service

import me.snitchon.documentation.Config
import me.snitchon.types.EndpointBundle
import me.snitchon.response.HttpResponse
import me.snitchon.request.RequestWrapper
import me.snitchon.Router
import me.snitchon.parsing.Parser
import kotlin.reflect.KClass

interface SnitchService {
    val config: Config get() = Config()
    fun registerMethod(endpointBundle: EndpointBundle<*>, path: String)
    fun setRoutes(routerConfiguration: Router.() -> Unit): RoutedService
    fun start()
    fun stop()

    fun <T : Exception, R : HttpResponse<*,*>> handleException(
        exceptionClass: KClass<T>,
        exceptionHandler: context(Parser) (T, RequestWrapper) -> R
    )
}