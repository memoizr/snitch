package me.snitchon

import me.snitchon.parameters.InvalidParametersException
import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.parsing.Parser
import me.snitchon.request.Body
import me.snitchon.request.HandlerResponse
import me.snitchon.request.RequestHandler
import me.snitchon.request.RequestWrapper
import me.snitchon.response.HttpResponse
import me.snitchon.service.Endpoint
import me.snitchon.service.SnitchService
import me.snitchon.types.HTTPMethod
import me.snitchon.types.StatusCodes
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

context (Parser)
class Router(
    val config: Config,
    val service: SnitchService,
    val pathParams: Set<PathParam<out Any, out Any>> = emptySet(),
) {
    val endpoints = mutableListOf<EndpointBundle<*>>()
    val parser = this@Parser

    fun GET() = Endpoint(HTTPMethod.GET, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun GET(path: String) = Endpoint(
        HTTPMethod.GET,
        null,
        null,
        path.leadingSlash,
        emptySet(),
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    infix fun GET(path: PathParam<out Any, out Any>) = GET("" / path)
    infix fun GET(path: ParametrizedPath) = Endpoint(
        HTTPMethod.GET,
        null,
        null,
        path.path.leadingSlash,
        path.pathParameters,
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    fun POST() = Endpoint(HTTPMethod.POST, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun POST(path: String) = Endpoint(
        HTTPMethod.POST,
        null,
        null,
        path.leadingSlash,
        emptySet(),
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    infix fun POST(path: PathParam<out Any, out Any>) = POST("" / path)
    infix fun POST(path: ParametrizedPath) = Endpoint(
        HTTPMethod.POST,
        null,
        null,
        path.path.leadingSlash,
        path.pathParameters,
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    fun PUT() = Endpoint(HTTPMethod.PUT, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun PUT(path: String) = Endpoint(
        HTTPMethod.PUT,
        null,
        null,
        path.leadingSlash,
        emptySet(),
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    infix fun PUT(path: PathParam<out Any, out Any>) = PUT("" / path)
    infix fun PUT(path: ParametrizedPath) = Endpoint(
        HTTPMethod.PUT,
        null,
        null,
        path.path.leadingSlash,
        path.pathParameters,
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    fun DELETE() = Endpoint(HTTPMethod.DELETE, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun DELETE(path: String) = Endpoint(
        HTTPMethod.DELETE,
        null,
        null,
        path.leadingSlash,
        emptySet(),
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    infix fun DELETE(path: PathParam<out Any, out Any>) = DELETE("" / path)
    infix fun DELETE(path: ParametrizedPath) = Endpoint(
        HTTPMethod.DELETE,
        null,
        null,
        path.path.leadingSlash,
        path.pathParameters,
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    fun OPTIONS() =
        Endpoint(HTTPMethod.OPTIONS, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))

    infix fun OPTIONS(path: String) = Endpoint(
        HTTPMethod.OPTIONS,
        null,
        null,
        path.leadingSlash,
        emptySet(),
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    infix fun OPTIONS(path: PathParam<out Any, out Any>) = OPTIONS("" / path)
    infix fun OPTIONS(path: ParametrizedPath) = Endpoint(
        HTTPMethod.OPTIONS,
        null,
        null,
        path.path.leadingSlash,
        path.pathParameters,
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    fun HEAD() = Endpoint(HTTPMethod.HEAD, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun HEAD(path: String) = Endpoint(
        HTTPMethod.HEAD,
        null,
        null,
        path.leadingSlash,
        emptySet(),
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    infix fun HEAD(path: PathParam<out Any, out Any>) = HEAD("" / path)
    infix fun HEAD(path: ParametrizedPath) = Endpoint(
        HTTPMethod.HEAD,
        null,
        null,
        path.path.leadingSlash,
        path.pathParameters,
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    fun PATCH() = Endpoint(HTTPMethod.PATCH, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun PATCH(path: String) = Endpoint(
        HTTPMethod.PATCH,
        null,
        null,
        path.leadingSlash,
        emptySet(),
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    infix fun PATCH(path: PathParam<out Any, out Any>) = PATCH("" / path)
    infix fun PATCH(path: ParametrizedPath) = Endpoint(
        HTTPMethod.PATCH,
        null,
        null,
        path.path.leadingSlash,
        path.pathParameters,
        emptySet(),
        emptySet(),
        Body(Nothing::class)
    )

    operator fun String.div(path: String) = this.leadingSlash + "/" + path
    operator fun String.div(path: PathParam<out Any, out Any>) = ParametrizedPath(this + "/{${path.name}}", setOf(path))

    operator fun String.div(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams)
        router.block()
        endpoints += router.endpoints.map {
            EndpointBundle(
                it.endpoint.copy(url = this.leadingSlash + it.endpoint.url),
                it.response,
                it.handler
            )
        }
    }

    operator fun String.invoke(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams)
        router.block()
        endpoints += router.endpoints.map {
            EndpointBundle(
                it.endpoint.copy(tags = it.endpoint.tags?.plus(this)),
                it.response,
                it.handler
            )
        }
    }

    operator fun ParametrizedPath.div(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams + this.pathParameters)
        router.block()
        router.endpoints += router.endpoints.map {
            EndpointBundle(
                it.endpoint.copy(
                    url = this.path.leadingSlash + it.endpoint.url,
                    pathParams = it.endpoint.pathParams + this.pathParameters
                ), it.response, it.handler
            )
        }
        endpoints += router.endpoints
    }

    operator fun PathParam<out Any, out Any>.div(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams + this)
        router.block()
        val path = ParametrizedPath("/{$name}", setOf(this))
        endpoints += router.endpoints.map {
            val url = path.path.leadingSlash + it.endpoint.url
            EndpointBundle(
                it.endpoint.copy(url = url, pathParams = it.endpoint.pathParams + this.copy(url)),
                it.response,
                it.handler
            )
        }
    }

    inline fun <B : Any, reified T : Any, S: StatusCodes> Endpoint<B>.addEndpoint(
        endpointResponse: HandlerResponse<B, T, S>
    ): Endpoint<B> {
        endpoints += EndpointBundle(this, EndpointResponse(endpointResponse.statusCodes, endpointResponse.type)) { request, response ->
            val invalidParams = request.getInvalidParams(pathParams, queryParams, headerParams)
            if (invalidParams.isNotEmpty()) {
                throw InvalidParametersException(invalidParams.foldRight(emptyList()) { error, acc -> acc + error })
            } else {
                before(request)
                endpointResponse.handler(
                    this@Parser,
                    RequestHandler(
                        body,
                        (headerParams + queryParams + pathParams),
                        request,
                        response
                    )
                ).also {
                    after(request, response)
                }
            }
        }
        return this
    }

    inline infix fun <B : Any, reified T : Any, S: StatusCodes> Endpoint<B>.isHandledBy(
        handlerResponse: HandlerResponse<B,T,S>
    ): Endpoint<B> = addEndpoint(handlerResponse)

    inline infix fun <B : Any, reified T : Any, reified S: StatusCodes> Endpoint<B>.isHandledBy(
        noinline block: context(Parser) RequestHandler<B>.() -> HttpResponse<T, S>
    ): Endpoint<B> = addEndpoint(
        HandlerResponse(S::class.starProjectedType, T::class.starProjectedType, block))

    data class EndpointBundle<T : Any>(
        val endpoint: Endpoint<T>,
        val response: EndpointResponse,
        val handler: (RequestWrapper, ResponseWrapper) -> HttpResponse<*, *>
    )

    data class EndpointResponse(
        val statusCode: KType,
        val type: KType
    )
}

internal val String.leadingSlash get() = if (!startsWith("/")) "/" + this else this
