package me.snitchon.router

import me.snitchon.parameters.PathParam
import me.snitchon.parsing.Parser
import me.snitchon.types.HandlerResponse
import me.snitchon.request.Context
import me.snitchon.response.HttpResponse
import me.snitchon.service.Endpoint
import me.snitchon.config.SnitchConfig
import me.snitchon.parameters.HeaderParameter
import me.snitchon.parameters.QueryParameter
import me.snitchon.request.Body
import me.snitchon.service.OpDescription
import me.snitchon.service.SnitchService
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
            before(request)
            endpointResponse.handler(
                parser,
                Context(request)
            ).also {
                after(request, it)
            }
        }
    }

    inline infix fun <B : Any, reified T : Any, S : StatusCodes> Endpoint<B>.isHandledBy(
        handlerResponse: HandlerResponse<B, T, S>
    ): Endpoint<B> = addEndpoint(handlerResponse)

    inline infix fun <B : Any, reified T : Any, reified S : StatusCodes> Endpoint<B>.isHandledBy(
        noinline handler: context(Parser) Context<B>.() -> HttpResponse<T, S>
    ): Endpoint<B> = addEndpoint(
        HandlerResponse(S::class.starProjectedType, T::class.starProjectedType, handler)
    )

    fun queries(vararg queryParameter: QueryParameter<*, *>) = queryParameter.asList()
    fun headers(vararg headerParameter: HeaderParameter<*, *>) = headerParameter.asList()
    fun description(description: String) = OpDescription(description)
    inline fun <reified T : Any> body(contentType: ContentType = ContentType.APPLICATION_JSON) =
        Body(T::class, contentType)

    fun apply(routerConfig: Router.() -> Unit, action: Endpoint<*>.() -> Endpoint<*>) {
        val router = Router(config, service, pathParams, parser, path)
        router.routerConfig()

        endpoints += router.endpoints.map {
            val endpoint = it.endpoint.action()
            EndpointBundle(
                endpoint,
                EndpointResponse(it.handlerResponse.statusCodes, it.handlerResponse.type),
                it.handlerResponse,
            ) { request ->
                endpoint.before(request)
                it.handlerResponse.handler(
                    parser,
                    Context(request)
                ).also {
                    endpoint.after(request, it)
                }
            }
        }
    }
}

fun (Router.() -> Unit).applyToAll(action: Endpoint<*>.() -> Endpoint<*>): Router.() -> Unit = {
    this@applyToAll(this.also { apply(this@applyToAll, action) })
}

fun routes(routes: Router.() -> Unit) = routes

internal val String.leadingSlash get() = if (!startsWith("/")) "/" + this else this
