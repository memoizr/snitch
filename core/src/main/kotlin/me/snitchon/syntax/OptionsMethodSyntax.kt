package me.snitchon.syntax

import me.snitchon.leadingSlash
import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.request.Body
import me.snitchon.service.Endpoint
import me.snitchon.types.HTTPMethod
import me.snitchon.types.HTTPMethod.OPTIONS

interface OptionsMethodSyntax : MethodSyntax {

    fun OPTIONS() = Endpoint(
        httpMethod = OPTIONS,
        summary = null,
        description = null,
        url = "",
        pathParams = pathParams,
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun OPTIONS(path: String) = Endpoint(
        httpMethod = OPTIONS,
        summary = null,
        description = null,
        url = path.leadingSlash,
        pathParams = emptySet(),
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun OPTIONS(path: ParametrizedPath) = Endpoint(
        httpMethod = OPTIONS,
        summary = null,
        description = null,
        url = path.path.leadingSlash,
        pathParams = path.pathParameters,
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun OPTIONS(path: PathParam<out Any, out Any>) = OPTIONS("" / path)
}