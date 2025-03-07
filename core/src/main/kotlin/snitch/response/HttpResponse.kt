package snitch.response

import snitch.types.Parser
import snitch.types.StatusCodes

sealed class HttpResponse<T, out S : StatusCodes> {
    abstract val statusCode: StatusCodes
    abstract val headers: Map<String, String>
    abstract val value: context(Parser) () -> Any?
    abstract fun header(header: Pair<String, String>): HttpResponse<T, S>

    fun map(
        failure: ErrorHttpResponse<T, *, S>.() -> HttpResponse<Any, *> = { this as HttpResponse<Any, *> },
        success: SuccessfulHttpResponse<T, S>.() -> HttpResponse<Any, *>,
    ): HttpResponse<Any, *> = when (this) {
        is SuccessfulHttpResponse -> this.success()
        is ErrorHttpResponse<T, *, S> -> this.failure()
        is RawHttpResponse -> throw UnsupportedOperationException()
    }
}
