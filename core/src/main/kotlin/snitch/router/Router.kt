package snitch.router

import snitch.parameters.HeaderParameter
import snitch.parameters.PathParam
import snitch.parameters.QueryParameter
import snitch.parsing.Parser
import snitch.request.Body
import snitch.request.TypedRequestWrapper
import snitch.response.HttpResponse
import snitch.service.*
import snitch.syntax.HttpMethodsSyntax
import snitch.types.*
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
    ): Endpoint<B> = apply {
        endpoints += EndpointBundle(
            this,
            EndpointResponse(endpointResponse.statusCodes, endpointResponse.type),
            endpointResponse as HandlerResponse<Any, Any, out StatusCodes>,
        ) { request ->
            decorator(
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

    internal fun applyToAll_(routerConfig: Routes, action: Endpoint<*>.() -> Endpoint<*>) {
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
    }
}

fun routes(routes: Routes) = routes

internal val String.leadingSlash get() = if (!startsWith("/")) "/$this" else this

fun Router.decorateWith(decoration: DecoratedWrapper.() -> HttpResponse<out Any, *>) = transformEndpoints { decorated(decoration) }
fun decoration(decoration: DecoratedWrapper.() -> HttpResponse<out Any, *>) = decoration
fun Router.transformEndpoints(action: Endpoint<*>.() -> Endpoint<*>): (Routes) -> Unit =
    { it: Routes -> applyToAll_(it, action) }
fun Router.onlyIf(condition: Condition) = transformEndpoints { onlyIf(condition) }
