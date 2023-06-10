package me.snitchon.syntax

import me.snitchon.EndpointBundle
import me.snitchon.Router
import me.snitchon.Routerable
import me.snitchon.leadingSlash
import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam

interface DivSyntax: Routerable {
    operator fun String.div(path: String) = this.leadingSlash + "/" + path
    operator fun String.div(path: PathParam<out Any, out Any>) = ParametrizedPath(this + "/{${path.name}}", setOf(path))

    operator fun String.div(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams, parser)
        router.block()
        endpoints += router.endpoints.map {
            EndpointBundle(
                it.endpoint.copy(url = this.leadingSlash + it.endpoint.url),
                it.response,
                it.handler
            )
        }
    }

    operator fun ParametrizedPath.div(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams + this.pathParameters, parser)
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
        val router = Router(config, service, pathParams + this, parser)
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

    operator fun String.invoke(block: Router.() -> Unit) {
        val router = Router(config, service, pathParams, parser)
        router.block()
        endpoints += router.endpoints.map {
            EndpointBundle(
                it.endpoint.copy(tags = it.endpoint.tags?.plus(this)),
                it.response,
                it.handler
            )
        }
    }
}