package com.snitch

import com.snitch.Format.*
import com.snitch.HttpResponse.*

sealed class HttpResponse<T>() {
    abstract val statusCode: Int
}

data class SuccessfulHttpResponse<T>(override val statusCode: Int,
                                     val body: T,
                                     val _format: Format = Json) : HttpResponse<T>()

data class ErrorHttpResponse<T, E>(override val statusCode: Int,
                                   val details: E) : HttpResponse<T>()
fun <T, R> HttpResponse<T>.mapSuccessful(fn: (T) -> R): HttpResponse<R> = if (this is SuccessfulHttpResponse) {
    SuccessfulHttpResponse(statusCode, fn(body), _format)
} else { this as HttpResponse<R> }

fun <T> T.success(code: Int = 200): HttpResponse<T> = SuccessfulHttpResponse(code, this)
val <T> T.ok: HttpResponse<T> get() = SuccessfulHttpResponse(200, this)
val <T> T.created: HttpResponse<T> get() = SuccessfulHttpResponse(201, this)
val <T> T.badRequest: HttpResponse<T> get() = ErrorHttpResponse(400, this)
val <T> T.forbidden: HttpResponse<T> get() = ErrorHttpResponse(403, this)
val <T> T.notFound: HttpResponse<T> get() = ErrorHttpResponse(403, this)
val Unit.noContent: HttpResponse<Unit> get() = SuccessfulHttpResponse(204, this)

fun <T> HttpResponse<T>.format(newFormat: Format) = if (this is SuccessfulHttpResponse) copy(_format = newFormat) else this

enum class Format(val type: String) {
    OctetStream("application/octect-streeam"),
    Json("application/json"),
    ImageJpeg("image/jpeg"),
    VideoMP4("video/mp4")
}
