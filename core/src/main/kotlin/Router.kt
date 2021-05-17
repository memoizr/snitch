package com.snitch

import SnitchService
import com.snitch.Format.*
import com.snitch.extensions.json
import com.google.gson.Gson
import kotlin.reflect.KClass


class Router (
    val config: Config,
    val service: SnitchService,
    private val pathParams: Set<PathParam<out Any, out Any>> = emptySet()
) {
//    val http = this
    val endpoints = mutableListOf<EndpointBundle<*>>()

    fun GET() = Endpoint(HTTPMethod.GET, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun GET(path: String) = Endpoint(HTTPMethod.GET, null, null, path.leadingSlash, emptySet(), emptySet(), emptySet(), Body(Nothing::class))
    infix fun GET(path: PathParam<out Any, out Any>) = GET("" / path)
    infix fun GET(path: ParametrizedPath) = Endpoint(HTTPMethod.GET, null, null, path.path.leadingSlash, path.pathParameters, emptySet(), emptySet(), Body(Nothing::class))

    fun POST() = Endpoint(HTTPMethod.POST, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun POST(path: String) = Endpoint(HTTPMethod.POST, null, null, path.leadingSlash, emptySet(), emptySet(), emptySet(), Body(Nothing::class))
    infix fun POST(path: PathParam<out Any, out Any>) = POST("" / path)
    infix fun POST(path: ParametrizedPath) = Endpoint(HTTPMethod.POST, null, null, path.path.leadingSlash, path.pathParameters, emptySet(), emptySet(), Body(Nothing::class))

    fun PUT() = Endpoint(HTTPMethod.PUT, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun PUT(path: String) = Endpoint(HTTPMethod.PUT, null, null, path.leadingSlash, emptySet(), emptySet(), emptySet(), Body(Nothing::class))
    infix fun PUT(path: PathParam<out Any, out Any>) = PUT("" / path)
    infix fun PUT(path: ParametrizedPath) = Endpoint(HTTPMethod.PUT, null, null, path.path.leadingSlash, path.pathParameters, emptySet(), emptySet(), Body(Nothing::class))

    fun DELETE() = Endpoint(HTTPMethod.DELETE, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun DELETE(path: String) = Endpoint(HTTPMethod.DELETE, null, null, path.leadingSlash, emptySet(), emptySet(), emptySet(), Body(Nothing::class))
    infix fun DELETE(path: PathParam<out Any, out Any>) = DELETE("" / path)
    infix fun DELETE(path: ParametrizedPath) = Endpoint(HTTPMethod.DELETE, null, null, path.path.leadingSlash, path.pathParameters, emptySet(), emptySet(), Body(Nothing::class))

    fun OPTIONS() = Endpoint(HTTPMethod.OPTIONS, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun OPTIONS(path: String) = Endpoint(HTTPMethod.OPTIONS, null, null, path.leadingSlash, emptySet(), emptySet(), emptySet(), Body(Nothing::class))
    infix fun OPTIONS(path: PathParam<out Any, out Any>) = OPTIONS("" / path)
    infix fun OPTIONS(path: ParametrizedPath) = Endpoint(HTTPMethod.OPTIONS, null, null, path.path.leadingSlash, path.pathParameters, emptySet(), emptySet(), Body(Nothing::class))

    fun HEAD() = Endpoint(HTTPMethod.HEAD, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun HEAD(path: String) = Endpoint(HTTPMethod.HEAD, null, null, path.leadingSlash, emptySet(), emptySet(), emptySet(), Body(Nothing::class))
    infix fun HEAD(path: PathParam<out Any, out Any>) = HEAD("" / path)
    infix fun HEAD(path: ParametrizedPath) = Endpoint(HTTPMethod.HEAD, null, null, path.path.leadingSlash, path.pathParameters, emptySet(), emptySet(), Body(Nothing::class))

    fun PATCH() = Endpoint(HTTPMethod.PATCH, null, null, "", pathParams, emptySet(), emptySet(), Body(Nothing::class))
    infix fun PATCH(path: String) = Endpoint(HTTPMethod.PATCH, null, null, path.leadingSlash, emptySet(), emptySet(), emptySet(), Body(Nothing::class))
    infix fun PATCH(path: PathParam<out Any, out Any>) = PATCH("" / path)
    infix fun PATCH(path: ParametrizedPath) = Endpoint(HTTPMethod.PATCH, null, null, path.path.leadingSlash, path.pathParameters, emptySet(), emptySet(), Body(Nothing::class))

    operator fun String.div(path: String) = this.leadingSlash + "/" + path
    operator fun String.div(path: PathParam<out Any, out Any>) = ParametrizedPath(this + "/{${path.name}}", setOf(path))

    operator fun String.div(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams)
        router.block()
        endpoints += router.endpoints.map { EndpointBundle(it.endpoint.copy(url = this.leadingSlash + it.endpoint.url), it.response, it.function) }
    }

    operator fun ParametrizedPath.div(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams + this.pathParameters)
        router.block()
        router.endpoints += router.endpoints.map { EndpointBundle(it.endpoint.copy(url = this.path.leadingSlash + it.endpoint.url, pathParams = it.endpoint.pathParams + this.pathParameters), it.response, it.function) }
        endpoints += router.endpoints
    }

    operator fun PathParam<out Any, out Any>.div(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams + this)
        router.block()
        val path = ParametrizedPath("/{$name}", setOf(this))
        endpoints += router.endpoints.map {
            val url = path.path.leadingSlash + it.endpoint.url
            EndpointBundle(it.endpoint.copy(url = url, pathParams = it.endpoint.pathParams + this.copy(url)), it.response, it.function)
        }
    }

    inline infix fun <B : Any, reified T : Any> Endpoint<B>.isHandledBy(noinline block: RequestHandler<B>.() -> HttpResponse<T>): Endpoint<B> {
        endpoints += EndpointBundle(this, T::class) { request, response ->
            val invalidParams = request.getInvalidParams(pathParams, queryParams, headerParams)
            if (invalidParams.isNotEmpty()) {
                response.setStatus(400)
                invalidParams.foldRight(emptyList<String>()) { error, acc -> acc + error }
                    .let { Gson().toJson(badRequest<T, List<String>>(it)) }
            } else try {
                block(RequestHandler(body, (headerParams + queryParams + pathParams), request, response)).let { httpResponse ->
                    response.setStatus(httpResponse.statusCode)
                    when (httpResponse) {
                        is HttpResponse.SuccessfulHttpResponse -> httpResponse.body.let {
                            response.setType(httpResponse._format)
                            when (httpResponse._format) {
                                Json -> it.json
                                OctetStream -> it
                                VideoMP4 -> it
                                ImageJpeg -> it
                            }
                        }
                        is HttpResponse.ErrorHttpResponse<*, *> -> httpResponse.json
                    }
                }
            } catch (unregisteredException: UnregisteredParamException) {
                val param = unregisteredException.param

                val type = when (param) {
                    is HeaderParameter -> "header"
                    is QueryParameter -> "query"
                    is PathParam -> "path"
                }
                HttpResponse.ErrorHttpResponse<T, String>(500, "Attempting to use unregistered $type parameter `${param.name}`").json
            }
        }
        return this
    }

    data class EndpointBundle<T : Any>(val endpoint: Endpoint<T>,
                                       val response: KClass<*>,
                                       val function: (RequestWrapper, ResponseWrapper) -> Any)
}

data class ParametrizedPath(val path: String, val pathParameters: Set<PathParam<out Any, out Any>>) {
    operator fun div(path: String) = copy(path = this.path + "/" + path)
    operator fun div(path: PathParam<out Any, out Any>) = copy(path = this.path + "/" + "{${path.name}}", pathParameters = pathParameters + path)
}

val String.leadingSlash get() = if (!startsWith("/")) "/" + this else this
