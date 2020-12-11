package com.snitch.spark

import RoutedService
import SnitchService
import ch.qos.logback.classic.Logger
import com.snitch.Config
import com.snitch.HTTPMethod
import com.snitch.Router
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Service
import java.io.File


class SparkSnitchService(override val config: Config) : SnitchService {
    val http by lazy { Service.ignite().port(config.port) }

    private val Router.EndpointBundle<*>.func: (request: Request, response: Response) -> Any
        get() =
            { request, response ->
                function(
                    SparkRequestWrapper(request),
                    SparkResponseWrapper(response)
                )
            }

    fun setRoutes(routerConfiguration: Router.() -> Unit): RoutedService {
        val tmpDir = File(System.getProperty("java.io.tmpdir") + "/swagger-ui/docs")
        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }
        http.externalStaticFileLocation(tmpDir.absolutePath)
        val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        logger.level = config.logLevel

        val router = Router(config, this)
        routerConfiguration(router)
        return RoutedService(this, router).startListening()
    }

    override fun registerMethod(it: Router.EndpointBundle<*>, path: String) {
        when (it.endpoint.httpMethod) {
            HTTPMethod.GET -> { http.get(path, it.func) }
            HTTPMethod.POST -> http.post(path, it.func)
            HTTPMethod.PUT -> http.put(path, it.func)
            HTTPMethod.PATCH -> http.patch(path, it.func)
            HTTPMethod.HEAD -> http.head(path, it.func)
            HTTPMethod.DELETE -> http.delete(path, it.func)
            HTTPMethod.OPTIONS -> http.options(path, it.func)
        }
    }

    fun stop() {
        http.stop()
    }
}

