package com.snitch.spark

import io.undertow.Handlers.exceptionHandler
import io.undertow.Undertow
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import io.undertow.server.handlers.ExceptionHandler
import io.undertow.util.HttpString
import io.undertow.util.Methods.*
import me.snitchon.*
import me.snitchon.documentation.ContentType
import me.snitchon.parsing.Parser
import me.snitchon.parsing.ParsingException
import me.snitchon.request.RequestWrapper
import me.snitchon.response.*
import me.snitchon.service.RoutedService
import me.snitchon.service.SnitchService
import me.snitchon.service.exceptionhandling.handleInvalidParameters
import me.snitchon.service.exceptionhandling.handleParsingException
import me.snitchon.service.exceptionhandling.handleUnregisteredParameters
import me.snitchon.types.*
import java.nio.ByteBuffer
import kotlin.reflect.KClass


@Suppress("UNCHECKED_CAST")
class UndertowSnitchService(override val config: Config, val parser: Parser) : SnitchService {

    private lateinit var service: Undertow
    private val handlers = mutableListOf<ExceptionHandler>()
    private val exceptionHandlers =
        LinkedHashMap<KClass<*>, context(Parser) (Exception, RequestWrapper) -> HttpResponse<*>>()

    private val routingHandler = RoutingHandler()
    private val serviceBuilder by lazy { Undertow.builder().addHttpListener(config.port, "localhost") }

    override fun setRoutes(routerConfiguration: Router.() -> Unit): RoutedService {
        val router = with(parser) { Router(config, this@UndertowSnitchService, emptySet()) }
        routerConfiguration(router)
        return RoutedService(this, router)
            .handleInvalidParameters()
            .handleUnregisteredParameters()
            .handleParsingException()
    }

    override fun start() {
        service = handlers
            .fold(serviceBuilder) { builder, routingHandler -> builder.setHandler(routingHandler) }
            .build()
        service.start()
    }

    override fun stop() {
        service.stop()
    }

    override fun <T : Exception, R : HttpResponse<*>> handleException(
        exceptionClass: KClass<T>,
        exceptionHandler: context (Parser) (T, RequestWrapper) -> R
    ) {
        exceptionHandlers[exceptionClass] = exceptionHandler as context(Parser) (Exception, RequestWrapper) -> R
    }

    override fun registerMethod(endpointBundle: Router.EndpointBundle<*>, path: String) {
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
                            ex as Exception,
                            UndertowRequestWrapper(exchange) { null })?.dispatch(exchange)
                    }
                })
        }
    }

    context (Parser)
    private val Router.EndpointBundle<*>.undertowHandler: (exchange: HttpServerExchange) -> Unit
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
    private fun Router.EndpointBundle<*>.handle(exchange: HttpServerExchange, b: () -> Any?) {
        handler(UndertowRequestWrapper(exchange, b), UndertowResponseWrapper(exchange))
            .dispatch(exchange)
    }

    context (Parser)
    private fun HttpResponse<*>.dispatch(exchange: HttpServerExchange) {
        when (this) {
            is SuccessfulHttpResponse<*> -> dispatchSuccessfulResponse(exchange)
            is ErrorHttpResponse<*, *> -> dispatchFailedResponse(exchange)
        }
    }

    context (Parser)
    private fun <T> ErrorHttpResponse<*, T>.dispatchFailedResponse(exchange: HttpServerExchange) {
        exchange.setStatusCode(this.statusCode)
        exchange.responseHeaders.put(HttpString("content-type"), Format.Json.type)
        exchange.responseSender.send(this.details?.serialized)
    }

    private fun SuccessfulHttpResponse<*>.dispatchSuccessfulResponse(exchange: HttpServerExchange) {
        exchange.setStatusCode(this.statusCode)
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

