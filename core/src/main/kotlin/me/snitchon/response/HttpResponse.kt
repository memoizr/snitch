package me.snitchon.response

import me.snitchon.parsing.Parser
import me.snitchon.types.StatusCodes

sealed class HttpResponse<T, out S : StatusCodes> {
    abstract val statusCode: StatusCodes
    abstract val headers: Map<String, String>
    abstract val value: context(Parser) () -> Any?

    fun map(
        failure: ErrorHttpResponse<T, *, S>.() -> HttpResponse<Any, *> = { this as HttpResponse<Any, *> },
        success: SuccessfulHttpResponse<T, S>.() -> HttpResponse<Any, *>,
    ): HttpResponse<Any, *> = when (this) {
        is SuccessfulHttpResponse -> this.success()
        is ErrorHttpResponse<T, *, S> -> this.failure()
    }

    abstract fun header(header: Pair<String, String>): HttpResponse<T, S>
}
