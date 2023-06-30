package me.snitch.syntax

import me.snitch.parameters.ParametrizedPath
import me.snitch.request.Body
import me.snitch.router.leadingSlash
import me.snitch.service.Endpoint
import me.snitch.types.HTTPMethods
import me.snitch.types.Routed

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
