package me.snitchon.request

import me.snitchon.parameters.*
import me.snitchon.response.CommonResponses

@JvmInline
value class Context<T : Any>(
    val request: ImplementationRequestWrapper,
) : CommonResponses {
    val body: T get() = request.body() as T
}

fun String?.filterValid(param: Parameter<*, *>): String? = when {
    this == null -> null
    param.emptyAsMissing && this.isEmpty() -> null
    param.invalidAsMissing && !param.pattern.regex.matches(this) -> null
    else -> this
}
