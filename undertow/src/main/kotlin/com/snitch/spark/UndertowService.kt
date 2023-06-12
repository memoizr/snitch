package com.snitch.spark

import io.undertow.Handlers.exceptionHandler
import io.undertow.Undertow
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import io.undertow.server.handlers.ExceptionHandler
import io.undertow.util.HttpString
import io.undertow.util.Methods.*
import me.snitchon.Router
import me.snitchon.parsing.Parser
import me.snitchon.request.RequestWrapper
import me.snitchon.response.ErrorHttpResponse
import me.snitchon.response.HttpResponse
import me.snitchon.response.SuccessfulHttpResponse
import me.snitchon.service.RoutedService
import me.snitchon.config.SnitchConfig
import me.snitchon.service.SnitchService
import me.snitchon.service.exceptionhandling.handleInvalidParameters
import me.snitchon.service.exceptionhandling.handleParsingException
import me.snitchon.service.exceptionhandling.handleUnregisteredParameters
import me.snitchon.types.ContentType
import me.snitchon.types.EndpointBundle
import me.snitchon.types.Format
import me.snitchon.types.HTTPMethods
import java.nio.ByteBuffer
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class UndertowSnitchService(
    private val parser: Parser,
    override val config: SnitchConfig = SnitchConfig()
) : SnitchService {

    private lateinit var service: Undertow
    private val handlers = mutableListOf<ExceptionHandler>()
    private val exceptionHandlers =
        LinkedHashMap<KClass<*>, context(Parser) RequestWrapper.(Exception) -> HttpResponse<*, *>>()

    private val routingHandler = RoutingHandler()
    private val serviceBuilder by lazy { Undertow.builder().addHttpListener(config.service.port, "localhost") }

    override fun setRoutes(routerConfiguration: Router.() -> Unit): RoutedService {
        val router = Router(config, this@UndertowSnitchService, emptySet(), parser)
        routerConfiguration(router)
        return RoutedService(
            service = this,
            router = router,
            onStart = ::start,
            onStop = ::stop
        )
            .handleInvalidParameters()
            .handleUnregisteredParameters()
            .handleParsingException()
    }

    override fun <T : Exception, R : HttpResponse<*, *>> handleException(
        exceptionClass: KClass<T>,
        exceptionHandler: context (Parser) RequestWrapper.(T) -> R
    ) {
        exceptionHandlers[exceptionClass] = exceptionHandler as context(Parser) RequestWrapper.(Exception) -> R
    }

    override fun registerMethod(endpointBundle: EndpointBundle<*>, path: String) {
        with(parser) {
            val handler = routingHandler.add(
                endpointBundle.endpoint.httpMethod.toUndertow(),
                path,
                endpointBundle.undertowHandler
            )

            handlers.add(exceptionHandler(handler)
                .also {
                    it.addExceptionHandler(Exception::class.java) { exchange ->
                        val ex: Throwable = exchange.getAttachment(ExceptionHandler.THROWABLE)
                        exceptionHandlers[ex::class]?.invoke(
                            parser,
                            UndertowRequestWrapper(parser, exchange) { null },
                            ex as Exception,
                        )?.dispatch(exchange)
                    }
                })
        }
    }

    private fun start() {
        service = handlers
            .fold(serviceBuilder) { builder, routingHandler -> builder.setHandler(routingHandler) }
            .build()
        service.start()
    }

    private fun stop() {
        service.stop()
    }

    context (Parser)
    private val EndpointBundle<*>.undertowHandler: (exchange: HttpServerExchange) -> Unit
        get() = { exchange: HttpServerExchange ->
            val byteArrayHandler = { byteArray: ByteArray? ->
                handle(exchange) {
                    with(parser) {
                        when (endpoint.body.contentType) {
                            ContentType.APPLICATION_JSON -> byteArray?.parse(endpoint.body.klass.java)
                            ContentType.APPLICATION_OCTET_STREAM -> byteArray
                            else -> byteArray.contentToString()
                        }
                    }
                }
            }
            if (endpoint.body.klass == Nothing::class) {
                byteArrayHandler(null)
            } else {
                exchange.requestReceiver.receiveFullBytes { _, byteArray -> byteArrayHandler(byteArray) }
            }
        }

    context (Parser)
    private fun EndpointBundle<*>.handle(exchange: HttpServerExchange, b: () -> Any?) {
        handler(UndertowRequestWrapper(parser, exchange, b), UndertowResponseWrapper(exchange))
            .dispatch(exchange)
    }

    context (Parser)
    private fun HttpResponse<*, *>.dispatch(exchange: HttpServerExchange) {
        when (this) {
            is SuccessfulHttpResponse<*, *> -> dispatchSuccessfulResponse(exchange)
            is ErrorHttpResponse<*, *, *> -> dispatchFailedResponse(exchange)
        }
    }

    context (Parser)
    private fun <T> ErrorHttpResponse<*, T, *>.dispatchFailedResponse(exchange: HttpServerExchange) {
        exchange.setStatusCode(this.statusCode.code)
        exchange.responseHeaders.put(HttpString("content-type"), Format.Json.type)
        exchange.responseSender.send(this.details?.serialized)
    }

    private fun SuccessfulHttpResponse<*, *>.dispatchSuccessfulResponse(exchange: HttpServerExchange) {
        exchange.setStatusCode(this.statusCode.code)
        exchange.responseHeaders.put(HttpString("content-type"), this._format.type)
        if (this._format == Format.Json) {
            val value1 = value(parser)!!

            exchange.responseSender.send(value1.toString())
            headers.forEach {
                exchange.responseHeaders.put(HttpString(it.key), it.value)
            }
        } else {
            if (body!!::class == ByteArray::class) {
                exchange.responseSender.send(ByteBuffer.wrap(body as ByteArray))
            } else {
                exchange.responseSender.send(body.toString())
            }
        }
    }

    private fun HTTPMethods.toUndertow() = when (this) {
        HTTPMethods.GET -> GET
        HTTPMethods.POST -> POST
        HTTPMethods.PUT -> PUT
        HTTPMethods.DELETE -> DELETE
        HTTPMethods.PATCH -> PATCH
        HTTPMethods.HEAD -> HEAD
        HTTPMethods.OPTIONS -> OPTIONS
    }
}

