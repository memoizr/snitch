package me.snitchon.syntax

import me.snitchon.parameters.ParametrizedPath
import me.snitchon.request.Body
import me.snitchon.router.leadingSlash
import me.snitchon.service.Endpoint
import me.snitchon.types.HTTPMethods
import me.snitchon.types.Routed

interface MethodSyntax : Routed, DivSyntax

internal fun MethodSyntax.method(httpMethods: HTTPMethods) = Endpoint(
    httpMethod = httpMethods,
    summary = null,
    description = null,
    path = this.path,
    pathParams = pathParams,
    queryParams = emptySet(),
    headerParams = emptySet(),
    body = Body(Nothing::class)
)

internal fun MethodSyntax.method(httpMethods: HTTPMethods, path: String) = Endpoint(
    httpMethod = httpMethods,
    summary = null,
    description = null,
    path = this.path + path.leadingSlash,
    pathParams = pathParams,
    queryParams = emptySet(),
    headerParams = emptySet(),
    body = Body(Nothing::class)
)

internal fun MethodSyntax.method(httpMethods: HTTPMethods, path: ParametrizedPath) = Endpoint(
    httpMethod = httpMethods,
    summary = null,
    description = null,
    path = this.path + path.path.leadingSlash,
    pathParams = pathParams + path.pathParameters,
    queryParams = emptySet(),
    headerParams = emptySet(),
    body = Body(Nothing::class)
)
