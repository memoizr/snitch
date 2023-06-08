package com.snitch.spark

import io.undertow.Handlers
import io.undertow.Undertow
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import io.undertow.server.handlers.ExceptionHandler
import io.undertow.util.HttpString
import io.undertow.util.Methods.*
import me.snitchon.*
import me.snitchon.parsing.Parser
import me.snitchon.parsing.ParsingException
import java.io.File
import java.text.ParseException
import kotlin.reflect.KClass


class UndertowSnitchService(override val config: Config, val parser: Parser) : SnitchService {

    private val handlers = mutableListOf<ExceptionHandler>()
    private val exceptionHandlers = mutableMapOf<KClass<*>, (Exception, RequestWrapper) -> HttpResponse<*>>()

    lateinit var service: Undertow

    private val routingHandler = RoutingHandler()
    private val serviceBuilder by lazy {
        Undertow
            .builder()
            .addHttpListener(config.port, "localhost")
    }

    private val Router.EndpointBundle<*>.func: (exchange: HttpServerExchange) -> Unit
        get() = { exchange: HttpServerExchange ->
            if (endpoint.body.klass == Nothing::class) {
                function(exchange, {null})
            } else {
                exchange.requestReceiver.receiveFullString { ex, msg ->
                    with(parser) {
                        function(exchange, {msg.parseJson(endpoint.body.klass.java)})
                    }
                }
            }
        }

    private inline fun Router.EndpointBundle<*>.function(
        exchange: HttpServerExchange,
        noinline b: () -> Any?
    ) {
        val result = function.invoke(
            UndertowRequestWrapper(exchange, b),
            UndertowResponseWrapper(exchange)
        )
        result.process(exchange)
    }

    fun setRoutes(routerConfiguration: Router.() -> Unit): RoutedService {
        val tmpDir = File(System.getProperty("java.io.tmpdir") + "/swagger-ui/docs")
        if (!tmpDir.exists()) {
            tmpDir.mkdirs()
        }

        val router = Router(config, this, emptySet(), parser)
        routerConfiguration(router)
        return RoutedService(this, router).handleException<ParsingException, _> { ex, req: RequestWrapper ->
            "Invalid body parameter"
                .badRequest
        }
    }

    fun HTTPMethod.toUndertow() = when (this) {
        HTTPMethod.GET -> GET
        HTTPMethod.POST -> POST
        HTTPMethod.PUT -> PUT
        HTTPMethod.DELETE -> DELETE
        HTTPMethod.PATCH -> PATCH
        HTTPMethod.HEAD -> HEAD
        HTTPMethod.OPTIONS -> OPTIONS
    }

    override fun registerMethod(it: Router.EndpointBundle<*>, path: String) {
        val handler: RoutingHandler =
            routingHandler.add(it.endpoint.httpMethod.toUndertow(), path, it.func)

        val x: ExceptionHandler = Handlers.exceptionHandler(handler)
            .also {
                it.addExceptionHandler(Exception::class.java) {
                    val ex: Throwable = it.getAttachment(ExceptionHandler.THROWABLE)
                    exceptionHandlers[ex::class]?.invoke(ex as Exception, UndertowRequestWrapper(it, {""}))
                        ?.process(it)
                }
            }

        handlers.add(x)
    }

    private inline fun HttpResponse<*>.process(exchange: HttpServerExchange) {
        when (this) {
            is SuccessfulHttpResponse<*> -> {
                exchange.setStatusCode(this.statusCode)
                exchange.responseHeaders.put(HttpString("Content-Type"), this._format.type)

                if (this._format == Format.Json) {
                    val body = this.body
                    with(parser) {
                        exchange.responseSender.send(body?.jsonString)
                    }
                } else {
                    exchange.responseSender.send(this.body.toString())
                }
            }

            is ErrorHttpResponse<*, *> -> {
                exchange.setStatusCode(this.statusCode)
                exchange.responseHeaders.put(HttpString("content-type"), Format.Json.type)
                with(parser) {
                    exchange.responseSender.send(this@process.details?.jsonString)
                }
            }
        }
    }


    override fun start() {
        val builder = handlers.fold(serviceBuilder) { acc, routingHandler -> acc.setHandler(routingHandler) }
        service = builder.build()
        service.start()
    }

    override fun <T : Exception, R : HttpResponse<*>> handleException(
        exception: KClass<T>,
        block: (T, RequestWrapper) -> R
    ) {
        exceptionHandlers.put(exception, block as (Exception, RequestWrapper) -> R)
    }

    override fun stop() {
        service.stop()
    }
}

