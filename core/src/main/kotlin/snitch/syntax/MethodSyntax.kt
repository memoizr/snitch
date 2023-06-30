package snitch.syntax

import snitch.parameters.ParametrizedPath
import snitch.request.Body
import snitch.router.leadingSlash
import snitch.service.Endpoint
import snitch.types.HTTPMethods
import snitch.types.Routed

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
