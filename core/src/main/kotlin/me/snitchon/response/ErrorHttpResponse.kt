package me.snitchon.response

import me.snitchon.parsing.Parser
import me.snitchon.types.StatusCodes

data class ErrorHttpResponse<T, E, out S : StatusCodes>(
    override val statusCode: StatusCodes,
    val details: E,
    override val value: context(Parser) () -> Any? = { details?.serialized },
    override val headers: Map<String, String> = emptyMap(),
) : HttpResponse<T, S>() {
    override fun header(header: Pair<String, String>): ErrorHttpResponse<T, E, S> =
        copy(headers = headers + header)
}