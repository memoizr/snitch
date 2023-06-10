package me.snitchon.syntax

import me.snitchon.leadingSlash
import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.request.Body
import me.snitchon.service.Endpoint
import me.snitchon.types.HTTPMethods.DELETE

interface DeleteMethodSyntax : MethodSyntax {

    fun DELETE() = Endpoint(
        httpMethod = DELETE,
        summary = null,
        description = null,
        url = "",
        pathParams = pathParams,
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun DELETE(path: String) = Endpoint(
        httpMethod = DELETE,
        summary = null,
        description = null,
        url = path.leadingSlash,
        pathParams = emptySet(),
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun DELETE(path: ParametrizedPath) = Endpoint(
        httpMethod = DELETE,
        summary = null,
        description = null,
        url = path.path.leadingSlash,
        pathParams = path.pathParameters,
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun DELETE(path: PathParam<out Any, out Any>) = DELETE("" / path)
}