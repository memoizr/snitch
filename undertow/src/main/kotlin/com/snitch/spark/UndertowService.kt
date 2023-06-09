package com.snitch.spark

import io.undertow.Handlers.exceptionHandler
import io.undertow.Undertow
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import io.undertow.server.handlers.ExceptionHandler
import io.undertow.util.HttpString
import io.undertow.util.Methods.*
import me.snitchon.*
import me.snitchon.parsing.Parser
import me.snitchon.parsing.ParsingException
import me.snitchon.types.ErrorResponse
import kotlin.reflect.KClass


class UndertowSnitchService(override val config: Config, val parser: Parser) : SnitchService {

    lateinit var service: Undertow

    private val handlers = mutableListOf<ExceptionHandler>()
    private val exceptionHandlers =
        LinkedHashMap<KClass<*>, context(Parser) (Exception, RequestWrapper) -> HttpResponse<*>>()

    private val routingHandler = RoutingHandler()
    private val serviceBuilder by lazy {
        Undertow
            .builder()
            .addHttpListener(config.port, "localhost")
    }

    override fun setRoutes(routerConfiguration: Router.() -> Unit): RoutedService {
        val router = with(parser) { Router(config, this@UndertowSnitchService, emptySet()) }
        routerConfiguration(router)
        return RoutedService(this, router).handleException<ParsingException, _> { ex, req: RequestWrapper ->
            ErrorResponse(400, "Invalid body parameter").badRequest
        }
    }

    override fun start() {
        val builder = handlers.fold(serviceBuilder) { acc, routingHandler -> acc.setHandler(routingHandler) }
        service = builder.build()
        service.start()
    }

    override fun stop() {
        service.stop()
    }

    override fun <T : Exception, R : HttpResponse<*>> handleException(
        exception: KClass<T>,
        block: context (Parser) (T, RequestWrapper) -> R
    ) {
//        with(parser) {
        exceptionHandlers.put(exception, block as context(Parser) (Exception, RequestWrapper) -> R)
//        }
    }

    override fun registerMethod(endpointBundle: Router.EndpointBundle<*>, path: String) {
        val handler: RoutingHandler =
            routingHandler.add(endpointBundle.endpoint.httpMethod.toUndertow(), path, endpointBundle.undertowHandler)

        handlers.add(exceptionHandler(handler)
            .also {
                it.addExceptionHandler(Exception::class.java) { exchange ->
                    val ex: Throwable = exchange.getAttachment(ExceptionHandler.THROWABLE)
                    exceptionHandlers[ex::class]?.invoke(
                        parser,
                        ex as Exception,
                        UndertowRequestWrapper(exchange) { null })
                        ?.dispatch(exchange)
                }
            })
    }

    private val Router.EndpointBundle<*>.undertowHandler: (exchange: HttpServerExchange) -> Unit
        get() = { exchange: HttpServerExchange ->
            val block = { byteArray: ByteArray? ->
                handle(exchange) { with(parser) { byteArray?.parseJson(endpoint.body.klass.java) } }
            }
            if (endpoint.body.klass == Nothing::class) {
                block(null)
            } else {
                exchange.requestReceiver.receiveFullBytes { ex, msg: ByteArray -> block(msg) }
            }
        }

    private fun Router.EndpointBundle<*>.handle(exchange: HttpServerExchange, b: () -> Any?) {
        handler(UndertowRequestWrapper(exchange, b), UndertowResponseWrapper(exchange))
            .dispatch(exchange)
    }

    private fun HttpResponse<*>.dispatch(exchange: HttpServerExchange) {
        when (this) {
            is SuccessfulHttpResponse<*> -> {
                exchange.setStatusCode(this.statusCode)
//                headers.forEach {
//                    exchange.responseHeaders.put(HttpString(it.key), it.value)
//                }
                exchange.responseHeaders.put(HttpString("Content-Type"), this._format.type)

                if (this._format == Format.Json) {
                    val body = this.body
                    with(parser) { exchange.responseSender.send(value(parser).toString()) }
                    headers.forEach {
                        exchange.responseHeaders.put(HttpString(it.key), it.value)
                    }
                } else {
//                    headers.forEach {
//                        exchange.responseHeaders.put(HttpString(it.key), it.value)
//                    }
                    exchange.responseSender.send(this.body.toString())
                }
            }

            is ErrorHttpResponse<*, *> -> {
                exchange.setStatusCode(this.statusCode)
                exchange.responseHeaders.put(HttpString("content-type"), Format.Json.type)
                with(parser) {
                    exchange.responseSender.send(this@dispatch.details?.jsonString)
                }
            }
        }
    }

    private fun HTTPMethod.toUndertow() = when (this) {
        HTTPMethod.GET -> GET
        HTTPMethod.POST -> POST
        HTTPMethod.PUT -> PUT
        HTTPMethod.DELETE -> DELETE
        HTTPMethod.PATCH -> PATCH
        HTTPMethod.HEAD -> HEAD
        HTTPMethod.OPTIONS -> OPTIONS
    }
}

