package me.snitchon.syntax

import me.snitchon.leadingSlash
import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.request.Body
import me.snitchon.service.Endpoint
import me.snitchon.types.HTTPMethods.*

interface GetMethodSyntax : MethodSyntax {

    fun GET() = Endpoint(
        httpMethod = GET,
        summary = null,
        description = null,
        url = path,
        pathParams = pathParams,
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun GET(path: String) = Endpoint(
        httpMethod = GET,
        summary = null,
        description = null,
        url = this.path + path.leadingSlash,
        pathParams = emptySet(),
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun GET(path: ParametrizedPath) = Endpoint(
        httpMethod = GET,
        summary = null,
        description = null,
        url = this.path + path.path.leadingSlash,
        pathParams = path.pathParameters,
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun GET(path: PathParam<out Any, out Any>) = GET("" / path)
}