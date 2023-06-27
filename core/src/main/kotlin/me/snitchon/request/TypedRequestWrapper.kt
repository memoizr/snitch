package me.snitchon.request

import me.snitchon.response.CommonResponses

@JvmInline
value class TypedRequestWrapper<T : Any>(
    val request: RequestWrapper,
): CommonResponses {
    val body: T get() = request.body() as T
}
