package me.snitchon.service

import me.snitchon.Router
import me.snitchon.extensions.print
import me.snitchon.parsing.Parser
import me.snitchon.request.ImplementationRequestWrapper
import me.snitchon.response.HttpResponse
import me.snitchon.types.ErrorResponse

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

        service.handleException(Exception::class) {
            it.printStackTrace()
            ErrorResponse(500, "Something went wrong").serverError
        }

        onStart()
        return this
    }

    fun stop() {
        onStop()
    }

    inline fun <reified T : Exception, R : HttpResponse<*, *>> handleException(noinline block: context(Parser) ImplementationRequestWrapper.(T) -> R): RoutedService {
        service.handleException(T::class, block)
        return this
    }
}