package me.snitchon.syntax

import me.snitchon.leadingSlash
import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.request.Body
import me.snitchon.service.Endpoint
import me.snitchon.types.HTTPMethods.HEAD

interface HeadMethodSyntax: MethodSyntax {

    fun HEAD() = Endpoint(
        httpMethod = HEAD,
        summary = null,
        description = null,
        url = this.path,
        pathParams = pathParams,
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun HEAD(path: String) = Endpoint(
        httpMethod = HEAD,
        summary = null,
        description = null,
        url = this.path + path.leadingSlash,
        pathParams = emptySet(),
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun HEAD(path: ParametrizedPath) = Endpoint(
        httpMethod = HEAD,
        summary = null,
        description = null,
        url = this.path + path.path.leadingSlash,
        pathParams = path.pathParameters,
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun HEAD(path: PathParam<out Any, out Any>) = HEAD(this.path / path)
}