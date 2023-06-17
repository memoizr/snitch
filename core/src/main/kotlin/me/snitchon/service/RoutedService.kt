package me.snitchon.service

import me.snitchon.router.Router
import me.snitchon.parsing.Parser
import me.snitchon.request.ImplementationRequestWrapper
import me.snitchon.response.HttpResponse
import me.snitchon.types.StatusCodes
import kotlin.reflect.KClass

data class RoutedService(
    val service: SnitchService,
    val router: Router,
    private val onStart: () -> Unit,
    private val onStop: () -> Unit,
) {
    fun start(): RoutedService {
        router.endpoints.forEach {
            val path: String = service.config.service.basePath + it.endpoint.url
            service.registerMethod(it, path)
        }

        onStart()
        return this
    }

    fun stop() {
        onStop()
    }

    inline fun <reified T : Throwable, R : HttpResponse<Any, StatusCodes>> handleException(noinline block: context(Parser) ImplementationRequestWrapper.(T) -> R): RoutedService {
        service.handleException(T::class, block)
        return this
    }

    fun <T : Throwable, R : HttpResponse<Any, StatusCodes>> handleException(ex: KClass<T>, block: context(Parser) ImplementationRequestWrapper.(T) -> R): RoutedService {
        service.handleException(ex, block)
        return this
    }
}