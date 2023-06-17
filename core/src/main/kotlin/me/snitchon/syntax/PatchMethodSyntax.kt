package me.snitchon.syntax

import me.snitchon.router.leadingSlash
import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.request.Body
import me.snitchon.service.Endpoint
import me.snitchon.types.HTTPMethods.PATCH

interface PatchMethodSyntax: MethodSyntax {

    fun PATCH() = Endpoint(
        httpMethod = PATCH,
        summary = null,
        description = null,
        url = this.path,
        pathParams = pathParams,
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun PATCH(path: String) = Endpoint(
        httpMethod = PATCH,
        summary = null,
        description = null,
        url = this.path + path.leadingSlash,
        pathParams = pathParams,
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun PATCH(path: ParametrizedPath) = Endpoint(
        httpMethod = PATCH,
        summary = null,
        description = null,
        url = this.path + path.path.leadingSlash,
        pathParams = pathParams + path.pathParameters,
        queryParams = emptySet(),
        headerParams = emptySet(),
        body = Body(Nothing::class)
    )

    infix fun PATCH(path: PathParam<out Any, out Any>) = PATCH("" / path)
}