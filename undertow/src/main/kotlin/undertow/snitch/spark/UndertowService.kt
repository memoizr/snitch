package undertow.snitch.spark

import io.undertow.Undertow
import io.undertow.server.HttpServerExchange
import io.undertow.server.RoutingHandler
import io.undertow.util.HttpString
import io.undertow.util.Methods.*
import me.snitch.router.Router
import me.snitch.config.SnitchConfig
import me.snitch.parsing.Parser
import me.snitch.request.RequestWrapper
import me.snitch.response.ErrorHttpResponse
import me.snitch.response.HttpResponse
import me.snitch.response.SuccessfulHttpResponse
import me.snitch.router.Routes
import me.snitch.service.RoutedService
import me.snitch.service.SnitchService
import me.snitch.service.exceptionhandling.handleInvalidParameters
import me.snitch.service.exceptionhandling.handleParsingException
import me.snitch.service.exceptionhandling.handleUnregisteredParameters
import me.snitch.types.ContentType
import me.snitch.types.EndpointBundle
import me.snitch.types.Format
import me.snitch.types.HTTPMethods
import java.nio.ByteBuffer
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

fun snitch(
    parser: Parser,
    config: SnitchConfig = SnitchConfig()
) = UndertowSnitchService(parser, config)

@Suppress("UNCHECKED_CAST")
class UndertowSnitchService(
    private val parser: Parser,
    override val config: SnitchConfig = SnitchConfig()
) : SnitchService {

    private var onStop: () -> Unit = {}
    private lateinit var service: Undertow
    private val handlers = mutableListOf<RoutingHandler>()
    private val exceptionHandlers =
        LinkedHashMap<KClass<*>, context(Parser) RequestWrapper.(Throwable) -> HttpResponse<*, *>>()

    private val routingHandler = RoutingHandler()
    private val serviceBuilder by lazy { Undertow.builder().addHttpListener(config.service.port, "localhost") }

    override fun onRoutes(routerConfiguration: Routes): RoutedService {
        val router = Router(config, this@UndertowSnitchService, emptySet(), parser, "")
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

    override fun onStop(action: () -> Unit): SnitchService = also {
        onStop = action
    }

    override fun <T : Throwable, R : HttpResponse<*, *>> handleException(
        exceptionClass: KClass<T>,
        exceptionHandler: context (Parser) RequestWrapper.(T) -> R
    ) {
        exceptionHandlers[exceptionClass] =
            exceptionHandler as context(Parser) RequestWrapper.(Throwable) -> R
    }

    override fun registerMethod(endpointBundle: EndpointBundle<*>, path: String) {
        with(parser) {
            handlers.add(
                routingHandler.add(
                    endpointBundle.endpoint.httpMethod.toUndertow(),
                    path,
                    endpointBundle.undertowHandler
                )
            )
        }
    }

    private fun start() {
        service = handlers
            .fold(serviceBuilder) { builder, routingHandler -> builder.setHandler(routingHandler) }
            .build()
        service.start()
    }

    private fun stop() {
        onStop()
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
        exchange.dispatch(Runnable {
            try {
                handler(UndertowRequestWrapper(parser, params, exchange, b))
                    .dispatch(exchange)
            } catch (ex: Throwable) {
                ex.printStackTrace()
                exceptionHandlers.keys
                    .find { ex::class.isSubclassOf(it) }?.let { exceptionHandlers[it] }
                    ?.invoke(
                        parser,
                        UndertowRequestWrapper(parser, params, exchange) { null },
                        ex,
                    )?.dispatch(exchange)
            }
        })
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
        headers.forEach {
            exchange.responseHeaders.put(HttpString(it.key), it.value)
        }
        if (this._format == Format.Json) {
            val value1 = value(parser)!!

            exchange.responseSender.send(value1.toString())
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

