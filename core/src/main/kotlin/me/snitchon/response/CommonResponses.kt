package me.snitchon.response

import me.snitchon.types.Format
import me.snitchon.types.StatusCodes

interface CommonResponses {
    fun <T, S : StatusCodes> HttpResponse<T, S>.format(newFormat: Format) =
        if (this is SuccessfulHttpResponse<T, S>) copy(_format = newFormat) else this

    val <T, S : StatusCodes> HttpResponse<T, S>.text get() =
        if (this is SuccessfulHttpResponse<T, S>) copy(_format = Format.TextPlain) else this

    fun <T, S : StatusCodes> HttpResponse<T, S>.serializer(serializer: (T) -> Any) =
        if (this is SuccessfulHttpResponse<T, S>) copy(value = { serializer(this.body) }) else this

    val <T> T.ok
        get() = SuccessfulHttpResponse(StatusCodes.OK, this)

    val <T> T.created: SuccessfulHttpResponse<T, StatusCodes.CREATED>
        get() = SuccessfulHttpResponse(StatusCodes.CREATED, this)

    val <T> T.accepted
        get() = SuccessfulHttpResponse(StatusCodes.ACCEPTED, this)

    val <T> T.noContent get() = SuccessfulHttpResponse(StatusCodes.NO_CONTENT, this)

    fun <T, E, S: StatusCodes> E.badRequest() = ErrorHttpResponse<T, _, S>(StatusCodes.BAD_REQUEST, this)

    fun <T, E, S: StatusCodes> E.unauthorized() = ErrorHttpResponse<T, _,S>(StatusCodes.UNAUTHORIZED, this)

    fun <T, E, S: StatusCodes> E.forbidden() = ErrorHttpResponse<T, _, S>(StatusCodes.FORBIDDEN, this)

    fun <T, E, S: StatusCodes> E.notFound() = ErrorHttpResponse<T, _, S>(StatusCodes.NOT_FOUND, this)

    fun <T, E, S: StatusCodes> E.serverError() = ErrorHttpResponse<T, _, S>(StatusCodes.INTERNAL_SERVER_ERROR, this)
}