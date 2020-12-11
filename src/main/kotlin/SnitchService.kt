import com.snitch.Config
import com.snitch.Router

interface SnitchService {
    val config: Config get() = Config()
    fun registerMethod(it: Router.EndpointBundle<*>, path: String)
}

data class RoutedService(val service: SnitchService, val router: Router) {
    fun startListening(): RoutedService {
        router.endpoints.forEach {
            val path: String = service.config.basePath + it.endpoint.url.replace("/{", "/:").replace("}", "")
            service.registerMethod(it, path)
        }
        return this
    }
}
