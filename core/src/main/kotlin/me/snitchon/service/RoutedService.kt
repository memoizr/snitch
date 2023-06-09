package me.snitchon.service

import me.snitchon.response.HttpResponse
import me.snitchon.request.RequestWrapper
import me.snitchon.Router
import me.snitchon.parsing.Parser

data class RoutedService(
    val service: SnitchService,
    val router: Router
) {
    fun startListening(): RoutedService {
        router.endpoints.forEach {
            val path: String = service.config.basePath + it.endpoint.url
            service.registerMethod(it, path)
        }

        service.start()
        return this
    }

    fun stopListening() {
        service.stop()
    }

    inline fun <reified T : Exception, R : HttpResponse<*>> handleException(noinline block: context(Parser) (T, RequestWrapper) -> R): RoutedService {
        service.handleException(T::class, block)
        return this
    }
}