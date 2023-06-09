package me.snitchon

import me.snitchon.Format.*
import me.snitchon.parsing.Parser

sealed class HttpResponse<T> {
    abstract val statusCode: Int
    abstract val headers: Map<String, String>
    abstract val value: context(Parser) () -> Any?
}
data class SuccessfulHttpResponse<T>(
    override val statusCode: Int,
    val body: T,
    val _format: Format = Json,
    override val value: context(Parser) () -> Any? = { body?.jsonString },
    override val headers: Map<String, String> = emptyMap(),
) : HttpResponse<T>()

data class ErrorHttpResponse<T, E>(
    override val statusCode: Int,
    val details: E,
    override val value: context(Parser) () -> Any? = { details?.jsonString },
    override val headers: Map<String, String> = emptyMap(),
) : HttpResponse<T>()

//fun <T, R> HttpResponse<T>.mapSuccessful(fn: (T) -> R): HttpResponse<R> = if (this is SuccessfulHttpResponse) {
//    SuccessfulHttpResponse(statusCode, fn(body), _format)
//} else {
//    this as HttpResponse<R>
//}

context (Parser)
fun <T> T.success(code: Int = 200): HttpResponse<T> = SuccessfulHttpResponse(code, this)
context (Parser)
inline val <reified T> T.ok: HttpResponse<T> get() = SuccessfulHttpResponse(200, this)
context (Parser)
val <T> T.created: HttpResponse<T> get() = SuccessfulHttpResponse(201, this)
context (Parser)
val <T> T.badRequest: HttpResponse<T> get() = ErrorHttpResponse(400, this)
context (Parser)
val <T> T.forbidden: HttpResponse<T> get() = ErrorHttpResponse(403, this)
context (Parser)
val Unit.noContent: HttpResponse<Unit> get() = SuccessfulHttpResponse(204, this)
context (Parser)
val <T> T.notFound: HttpResponse<T> get() = ErrorHttpResponse(404, this)
context (Parser)
val <T> T.serverError: HttpResponse<T> get() = ErrorHttpResponse(500, this)

context (Parser)
fun <T> T.ok(): HttpResponse<T> = SuccessfulHttpResponse(200, this)
context (Parser)
fun <T> T.created(): HttpResponse<T> = SuccessfulHttpResponse(201, this)
context (Parser)
fun <T, E> T.badRequest(): HttpResponse<T> = ErrorHttpResponse(400, this)
context (Parser)
fun <T, E> T.forbidden(): HttpResponse<T> = ErrorHttpResponse(403, this)
context (Parser)
fun <T, E> T.notFound(): HttpResponse<T> = ErrorHttpResponse(404, this)
context (Parser)
fun Unit.noContent(): HttpResponse<Unit> = SuccessfulHttpResponse(204, this)

context (Parser)
fun <T> HttpResponse<T>.format(newFormat: Format) =
    if (this is SuccessfulHttpResponse) copy(_format = newFormat) else this

context (Parser)
fun <T> HttpResponse<T>.serializer(serializer: (T) -> Any) =
    if (this is SuccessfulHttpResponse) copy(value = {serializer(this.body)}) else this

enum class Format(val type: String) {
    OctetStream("application/octect-streeam"),
    Json("application/json"),
    ImageJpeg("image/jpeg"),
    VideoMP4("video/mp4"),
    TextHTML("text/html"),
    TextPlain("text/plain"),
}
