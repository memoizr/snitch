package me.snitchon.syntax

import me.snitchon.types.EndpointBundle
import me.snitchon.Router
import me.snitchon.types.Routed
import me.snitchon.leadingSlash
import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam

interface DivSyntax: Routed {
    operator fun String.div(path: String) = this.leadingSlash + "/" + path
    operator fun String.div(path: PathParam<out Any, out Any>) = ParametrizedPath(this + "/{${path.name}}", setOf(path))

    operator fun PathParam<out Any, out Any>.div(string: String) = ParametrizedPath("/{${name}}" + string.leadingSlash, pathParams + this)

    operator fun String.div(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams, parser, path + this.leadingSlash)
        router.block()
        endpoints += router.endpoints.map {
            EndpointBundle(
                it.endpoint,
                it.response,
                it.handlerResponse,
                it.handler,
            )
        }
    }

    operator fun ParametrizedPath.div(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams + this.pathParameters, parser, this@DivSyntax.path + this.path.leadingSlash)
        router.block()
        router.endpoints += router.endpoints.map {
            EndpointBundle(
                it.endpoint,
                it.response,
                it.handlerResponse,
                it.handler
            )
        }
        endpoints += router.endpoints
    }

    operator fun PathParam<out Any, out Any>.div(block: Router.() -> Unit) {
        val path = ParametrizedPath("/{$name}", setOf(this))
        val router = Router(config, service, pathParams + this, parser, this@DivSyntax.path + path.path.leadingSlash)
        router.block()
        endpoints += router.endpoints.map {
            EndpointBundle(
                it.endpoint.copy(pathParams = it.endpoint.pathParams),
                it.response,
                it.handlerResponse,
                it.handler
            )
        }
    }
}