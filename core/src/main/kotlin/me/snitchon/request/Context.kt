package me.snitchon.request

import me.snitchon.parameters.*
import me.snitchon.response.CommonResponses

@JvmInline
value class Context<T : Any>(
    val request: ImplementationRequestWrapper,
) : CommonResponses {
    val body: T get() = request.body() as T
}
