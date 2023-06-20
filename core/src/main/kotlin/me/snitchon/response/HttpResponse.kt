package me.snitchon.response

import me.snitchon.parsing.Parser
import me.snitchon.types.Format
import me.snitchon.types.Format.*
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
}

data class SuccessfulHttpResponse<T, out S : StatusCodes>(
    override val statusCode: S,
    val body: T,
    val _format: Format = Json,
    override val value: context(Parser) () -> Any? = {
        when (_format) {
            OctetStream -> body
            Json -> body?.serialized
            ImageJpeg -> body
            VideoMP4 -> body
            TextHTML -> body
            TextPlain -> body
        }
    },
    override val headers: Map<String, String> = emptyMap(),
) : HttpResponse<T, S>()

data class ErrorHttpResponse<T, E, out S : StatusCodes>(
    override val statusCode: StatusCodes,
    val details: E,
    override val value: context(Parser) () -> Any? = { details?.serialized },
    override val headers: Map<String, String> = emptyMap(),
) : HttpResponse<T, S>()
