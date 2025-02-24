package snitch.router

import snitch.parameters.HeaderParameter
import snitch.parameters.PathParam
import snitch.parameters.QueryParameter
import snitch.request.Body
import snitch.request.TypedRequestWrapper
import snitch.response.HttpResponse
import snitch.service.Condition
import snitch.service.DecoratedWrapper
import snitch.service.Endpoint
import snitch.service.OpDescription
import snitch.service.SnitchService
import snitch.syntax.HttpMethodsSyntax
import snitch.types.ContentType
import snitch.types.EndpointBundle
import snitch.types.EndpointResponse
import snitch.types.HandlerResponse
import snitch.types.Parser
import snitch.types.StatusCodes
import kotlin.reflect.full.starProjectedType

class Router(
    override val config: snitch.config.SnitchConfig,
    override val service: SnitchService,
    override val pathParams: Set<PathParam<out Any, out Any>> = emptySet(),
    override val parser: Parser,
    override val path: String,
) : HttpMethodsSyntax {

    override val endpoints = mutableListOf<EndpointBundle<*>>()

    inline fun <B : Any, reified T : Any, S : StatusCodes> Endpoint<B>.addEndpoint(
        endpointResponse: HandlerResponse<B, T, S>
    ): Endpoint<B> = applyConditions()
        .also {
            endpoints += EndpointBundle(
                it,
                EndpointResponse(endpointResponse.statusCodes, endpointResponse.type),
                endpointResponse as HandlerResponse<Any, Any, out StatusCodes>,
            ) { request ->
                it.decorator(
                    DecoratedWrapper({
                        endpointResponse.handler(
                            TypedRequestWrapper(request)
                        )
                    }, request)
                ).next()
            }
        }

    inline infix fun <B : Any, reified T : Any, S : StatusCodes> Endpoint<B>.isHandledBy(
        handlerResponse: HandlerResponse<B, T, S>
    ): Endpoint<B> = addEndpoint(handlerResponse)

    inline infix fun <B : Any, reified T : Any, reified S : StatusCodes> Endpoint<B>.isHandledBy(
        noinline handler: TypedRequestWrapper<B>.() -> HttpResponse<T, S>
    ): Endpoint<B> = addEndpoint(
        HandlerResponse(S::class.starProjectedType, T::class.starProjectedType, handler)
    )

    fun queries(vararg queryParameter: QueryParameter<*, *>) = queryParameter.asList()
    fun headers(vararg headerParameter: HeaderParameter<*, *>) = headerParameter.asList()
    fun description(description: String) = OpDescription(description)
    inline fun <reified T : Any> body(contentType: ContentType = ContentType.APPLICATION_JSON) =
        Body(T::class, contentType)

    internal fun applyToAll_(routerConfig: Routes, action: Endpoint<*>.() -> Endpoint<*>): Router {
        val router = Router(config, service, pathParams, parser, path)
        router.routerConfig()

        endpoints += router.endpoints.map {
            val endpoint = it.endpoint.action()
            EndpointBundle(
                endpoint,
                EndpointResponse(it.handlerResponse.statusCodes, it.handlerResponse.type),
                it.handlerResponse,
            ) { request ->
                endpoint.decorator(
                    DecoratedWrapper({
                        it.handlerResponse.handler(
                            TypedRequestWrapper(request)
                        )
                    }, request)
                ).next()
            }
        }
        return router
    }
}

fun routes(vararg tags: String, routes: Routes): Router.() -> Unit = {
    val router = Router(config, service, pathParams, parser, path)
    router.routes()
    router.endpoints.replaceAll {
        (it as EndpointBundle<Any>).copy(
            endpoint = it.endpoint.copy(tags = it.endpoint.tags.orEmpty() + tags.toList())
        )
    }
    endpoints.addAll(router.endpoints)

//    this.endpoints.replaceAll {
//        (it as EndpointBundle<Any>).copy(
//            endpoint = it.endpoint
//                .copy(
//                    tags = listOf(it.endpoint.path.split("/").drop(3).first()) + (it.endpoint.tags ?: emptyList()),
//                )
//        )
//    }
}
//fun routes(routes: Routes): Router.() -> Unit = routes

internal val String.leadingSlash get() = if (!startsWith("/")) "/$this" else this

fun decorateWith(decoration: DecoratedWrapper.() -> HttpResponse<out Any, StatusCodes>): Router.(Router.() -> Unit) -> Router =
    transformEndpoints { decorated(decoration) }

fun decoration(decoration: DecoratedWrapper.() -> HttpResponse<out Any, *>) = decoration
fun transformEndpoints(action: Endpoint<*>.() -> Endpoint<*>): Router.(Routes) -> Router =
    { it: Routes -> this.applyToAll_(it, action) }

fun Router.onlyIf(condition: Condition) = transformEndpoints { onlyIf(condition) }

operator fun (Router.(Router.() -> Unit) -> Router).plus(
    other: Router.(Router.() -> Unit) -> Router
): Router.(Router.() -> Unit) -> Router = {
    this@plus({other(it)})
}
