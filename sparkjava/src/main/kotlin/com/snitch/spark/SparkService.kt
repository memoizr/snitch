package com.snitch.spark

import me.snitchon.parsing.Parser
import ch.qos.logback.classic.Logger
import me.snitchon.*
import me.snitchon.documentation.Config
import me.snitchon.request.RequestWrapper
import me.snitchon.service.RoutedService
import me.snitchon.service.SnitchService
import me.snitchon.response.ErrorHttpResponse
import me.snitchon.types.HTTPMethod
import me.snitchon.response.HttpResponse
import me.snitchon.response.SuccessfulHttpResponse
import me.snitchon.types.EndpointBundle
import org.slf4j.LoggerFactory
import spark.Request
import spark.Response
import spark.Service
import java.io.File
import kotlin.reflect.KClass


class SparkSnitchService(
    override val config: Config,
    val parser: Parser
) : SnitchService {
    val http by lazy { Service.ignite().port(config.port) }

    private val EndpointBundle<*>.func: (request: Request, response: Response) -> Any
        get() =
            { request, response ->
                handler(
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

        val router = Router(config, this, emptySet(), parser)
        routerConfiguration(router)
        return RoutedService(this, router).startListening()
    }

    override fun registerMethod(it: EndpointBundle<*>, path: String) {
        val sparkPath = path.replace("{", ":").replace("}", "")
        when (it.endpoint.httpMethod) {
            HTTPMethod.GET -> {
                http.get(sparkPath, it.func)
            }

            HTTPMethod.POST -> http.post(sparkPath, it.func)
            HTTPMethod.PUT -> http.put(sparkPath, it.func)
            HTTPMethod.PATCH -> http.patch(sparkPath, it.func)
            HTTPMethod.HEAD -> http.head(sparkPath, it.func)
            HTTPMethod.DELETE -> http.delete(sparkPath, it.func)
            HTTPMethod.OPTIONS -> http.options(sparkPath, it.func)
        }
    }

    override fun start() {
        http.awaitInitialization()
    }

    override fun <T : Exception, R : HttpResponse<*>> handleException(
        exception: KClass<T>,
        block: (T, RequestWrapper) -> R
    ) {
        http.exception(exception.java) { ex, req, res ->
            val handled = block(ex, SparkRequestWrapper(req))
            res.status(handled.statusCode)
            when (handled) {
                is SuccessfulHttpResponse<*> -> res.body(with(parser) { handled.body!!.serialized })
                is ErrorHttpResponse<*, *> -> res.body(with(parser) { handled.details!!.serialized })
            }
        }
    }

    override fun stop() {
        http.stop()
    }
}

