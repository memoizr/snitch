package me.snitchon.router

import me.snitchon.parameters.PathParam
import me.snitchon.parsing.Parser
import me.snitchon.types.HandlerResponse
import me.snitchon.request.TypedRequestWrapper
import me.snitchon.response.HttpResponse
import me.snitchon.config.SnitchConfig
import me.snitchon.parameters.HeaderParameter
import me.snitchon.parameters.QueryParameter
import me.snitchon.request.Body
import me.snitchon.service.*
import me.snitchon.syntax.HttpMethodsSyntax
import me.snitchon.types.ContentType
import me.snitchon.types.EndpointBundle
import me.snitchon.types.EndpointResponse
import me.snitchon.types.StatusCodes
import kotlin.reflect.full.starProjectedType

class Router(
    override val config: SnitchConfig,
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
                        parser,
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
        noinline handler: context(Parser) TypedRequestWrapper<B>.() -> HttpResponse<T, S>
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
                            parser,
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

fun Router.using(decoration: DecoratedWrapper.() -> HttpResponse<out Any, *>) = transformEndpoints { decorate(decoration) }
fun Router.transformEndpoints(action: Endpoint<*>.() -> Endpoint<*>): (Routes) -> Unit =
    { it: Routes -> applyToAll_(it, action) }
fun Router.onlyIf(condition: Condition) = transformEndpoints { onlyIf(condition) }
