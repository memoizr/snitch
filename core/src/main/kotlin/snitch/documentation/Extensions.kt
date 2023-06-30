package snitch.documentation

import snitch.types.ContentType
import snitch.types.HTTPMethods
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.starProjectedType

internal fun OpenApi.withPath(name: String, path: Path) =
    copy(paths = paths + (name to path))

internal fun Path.withOperation(method: HTTPMethods, operation: Operation) = when (method) {
    HTTPMethods.GET -> copy(get = operation)
    HTTPMethods.POST -> copy(post = operation)
    HTTPMethods.DELETE -> copy(delete = operation)
    HTTPMethods.PUT -> copy(put = operation)
    HTTPMethods.PATCH -> copy(patch = operation)
    HTTPMethods.HEAD -> copy(head = operation)
    HTTPMethods.OPTIONS -> copy(options = operation)
}

internal fun Operation.withParameter(parameter: Parameters.Parameter) =
    copy(parameters = (parameters ?: emptyList()) + parameter)

internal fun Operation.withRequestBody(
    documentationSerializer: DocumentationSerializer,
    contentType: ContentType,
    body: KClass<*>
) =
    copy(
        requestBody = RequestBodies.RequestBody(
            content =
            mapOf(contentType.value to MediaType(toSchema(documentationSerializer, body.starProjectedType)))
        )
    )

internal fun Operation.withResponse(
    documentationSerializer: DocumentationSerializer,
    contentType: ContentType,
    body: KType,
    code: String
) = copy(
    responses = responses + (code to Responses.Response(
        content = mapOf(
            contentType.value to MediaType(
                toSchema(documentationSerializer, body)
            )
        )
    ))
)
