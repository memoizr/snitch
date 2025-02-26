package snitch.response

import snitch.types.Format
import snitch.types.Parser
import snitch.types.StatusCodes

data class SuccessfulHttpResponse<T, out S : StatusCodes>(
    override val statusCode: S,
    val body: T,
    val _format: Format = Format.Json,
    override val value: context(Parser) () -> Any? = {
        when (_format) {
            Format.Json -> body?.serialized
            Format.TextHTML -> body
            Format.OctetStream -> body
            Format.TextPlain -> body
            Format.ImageJpeg -> body
            Format.VideoMP4 -> body
        }
    },
    override val headers: Map<String, String> = emptyMap(),
) : HttpResponse<T, S>() {
    override fun header(header: Pair<String, String>): SuccessfulHttpResponse<T, S> =
        copy(headers = headers + header)
}

data class RawHttpResponse<T, out S : StatusCodes>(
    override val statusCode: S,
    val body: Any,
    val _format: Format = Format.Json,
    override val value: context(Parser) () -> Any? = { body },
    override val headers: Map<String, String> = emptyMap(),
) : HttpResponse<T, S>() {
    override fun header(header: Pair<String, String>) = copy(headers = headers + header)
}
