package me.snitchon

import kotlin.reflect.KType

data class EndpointResponse(
    val statusCode: KType,
    val type: KType
)