package me.snitchon

import kotlin.reflect.KClass

interface SnitchService {
    val config: Config get() = Config()
    fun registerMethod(endpointBundle: Router.EndpointBundle<*>, path: String)
    fun stop()
    fun start()
    fun <T: Exception, R: HttpResponse<*>> handleException(exception: KClass<T>, block: (T, RequestWrapper) -> R)
}

data class RoutedService(val service: SnitchService,
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

    inline fun <reified T: Exception, R: HttpResponse<*>> handleException(noinline block: (T, RequestWrapper) -> R): RoutedService {
        service.handleException(T::class, block)
        return this
    }
}
