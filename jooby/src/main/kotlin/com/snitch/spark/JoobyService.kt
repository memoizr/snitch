package com.snitch.spark

import RoutedService
import SnitchService
import com.snitch.Config
import com.snitch.HTTPMethod
import com.snitch.Router
import io.jooby.Context
import io.jooby.Jooby
import io.jooby.ServerOptions
import io.jooby.runApp
import java.io.File
import java.util.function.Consumer
import java.util.function.Supplier


class App : Jooby() {
}

class JoobySnitchService(override val config: Config) : SnitchService {
    val service by lazy { App().also { it.setServerOptions(
        ServerOptions()
            .setPort(config.port)
    ) } }

    private val Router.EndpointBundle<*>.func: (context: Context) -> Any
        get() =
            { context ->
                function(
                    JoobyRequestWrapper(context),
                    JoobyResponseWrapper(context)
                )
            }

    fun setRoutes(routerConfiguration: Router.() -> Unit): RoutedService {
        val tmpDir = File(System.getProperty("java.io.tmpdir") + "/swagger-ui/docs")
        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }
//        http.externalStaticFileLocation(tmpDir.absolutePath)

        val router = Router(config, this)
        routerConfiguration(router)
        val routedService = RoutedService(this, router)
        Jooby.runApp(emptyArray<String>().also { println("args") }, Supplier {

            println("here")
            routedService.startListening()
//            service.ini
            service
        })


        return routedService
    }

    override fun registerMethod(it: Router.EndpointBundle<*>, path: String) {
        when (it.endpoint.httpMethod) {
            HTTPMethod.GET -> { service.get(path, it.func) }
            HTTPMethod.POST -> service.post(path, it.func)
            HTTPMethod.PUT -> service.put(path, it.func)
            HTTPMethod.PATCH -> service.patch(path, it.func)
            HTTPMethod.HEAD -> service.head(path, it.func)
            HTTPMethod.DELETE -> service.delete(path, it.func)
            HTTPMethod.OPTIONS -> service.options(path, it.func)
        }
    }

    fun stop() {
        service.stop()
    }
}

