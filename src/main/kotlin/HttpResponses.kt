package com.snitch

import com.snitch.Format.*
import com.snitch.HttpResponse.*

sealed class HttpResponse<T>() {
    abstract val statusCode: Int

    data class SuccessfulHttpResponse<T>(override val statusCode: Int,
                                         val body: T,
                                         val _format: Format = Json) : HttpResponse<T>()

    data class ErrorHttpResponse<T, E>(override val statusCode: Int,
                                       val details: E) : HttpResponse<T>()
}

fun <T> T.success(code: Int = 200): HttpResponse<T> = HttpResponse.SuccessfulHttpResponse(code, this)
val <T> T.ok: HttpResponse<T> get() = HttpResponse.SuccessfulHttpResponse(200, this)
val <T> T.created: HttpResponse<T> get() = HttpResponse.SuccessfulHttpResponse(201, this)
fun <T, E> badRequest(body: E, code: Int = 400) = HttpResponse.ErrorHttpResponse<T, E>(code, body)
fun <T> forbidden(message: String) = HttpResponse.ErrorHttpResponse<T, String>(403, message)
fun <T> notFound() = HttpResponse.ErrorHttpResponse<T, String>(404, "not found")

fun <T> HttpResponse<T>.format(newFormat: Format) = if (this is SuccessfulHttpResponse) copy(_format = newFormat) else this

enum class Format(val type: String) {
    OctetStream("application/octect-streeam"),
    Json("application/json"),
    ImageJpeg("image/jpeg"),
    VideoMP4("video/mp4")
}
