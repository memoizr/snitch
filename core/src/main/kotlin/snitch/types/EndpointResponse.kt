package snitch.types

import kotlin.reflect.KType

data class EndpointResponse(
    val statusCode: KType,
    val type: KType
)