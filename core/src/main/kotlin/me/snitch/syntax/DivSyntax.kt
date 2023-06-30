package me.snitch.syntax

import me.snitch.types.EndpointBundle
import me.snitch.router.Router
import me.snitch.types.Routed
import me.snitch.router.leadingSlash
import me.snitch.parameters.ParametrizedPath
import me.snitch.parameters.PathParam
import me.snitch.router.Routes

interface DivSyntax: Routed {
    operator fun String.div(path: String) = this.leadingSlash + "/" + path
    operator fun String.div(path: PathParam<out Any, out Any>) = ParametrizedPath(this + "/{${path.name}}", setOf(path))

    operator fun PathParam<out Any, out Any>.div(string: String) = ParametrizedPath("/{${name}}" + string.leadingSlash, pathParams + this)

    operator fun String.div(block: Routes) {
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

    operator fun ParametrizedPath.div(block: Routes) {
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

    operator fun PathParam<out Any, out Any>.div(block: Routes) {
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