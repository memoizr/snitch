package com.snitch.documentation

import com.snitch.HTTPMethod
import kotlin.reflect.KClass
import kotlin.reflect.full.starProjectedType

internal fun OpenApi.withPath(name: String, path: Path) =
        copy(paths = paths + (name to path))

internal fun Path.withOperation(method: HTTPMethod, operation: Operation) = when (method) {
    HTTPMethod.GET -> copy(get = operation)
    HTTPMethod.POST -> copy(post = operation)
    HTTPMethod.DELETE -> copy(delete = operation)
    HTTPMethod.PUT -> copy(put = operation)
    HTTPMethod.PATCH -> copy(patch = operation)
    HTTPMethod.HEAD -> copy(head = operation)
    HTTPMethod.OPTIONS -> copy(options = operation)
}

internal fun Operation.withParameter(parameter: Parameters.Parameter) =
        copy(parameters = (parameters ?: emptyList()) + parameter)

internal fun Operation.withRequestBody(
        contentType: ContentType,
        body: KClass<*>) =
        copy(requestBody = RequestBodies.RequestBody(content =
        mapOf(contentType.value to MediaType(toSchema(body.starProjectedType)))))

internal fun Operation.withResponse(
        contentType: ContentType,
        body: KClass<*>,
        code: String = "200"
) = copy(responses = responses + (code to Responses.Response(
        content = mapOf(contentType.value to MediaType(
                toSchema(body.starProjectedType)
        )))))
