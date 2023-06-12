package me.snitchon.response

import me.snitchon.types.Format
import me.snitchon.types.StatusCodes

interface CommonResponses {
    fun <T, S : StatusCodes> HttpResponse<T, S>.format(newFormat: Format) =
        if (this is SuccessfulHttpResponse<T, S>) copy(_format = newFormat) else this

    fun <T, S : StatusCodes> HttpResponse<T, S>.serializer(serializer: (T) -> Any) =
        if (this is SuccessfulHttpResponse<T, S>) copy(value = { serializer(this.body) }) else this

    val <T> T.ok
        get() = SuccessfulHttpResponse(StatusCodes.OK, this)

    val <T> T.created
        get() = SuccessfulHttpResponse(StatusCodes.CREATED, this)

    val <T> T.accepted
        get() = SuccessfulHttpResponse(StatusCodes.ACCEPTED, this)

    val <T> T.noContent
        get() = SuccessfulHttpResponse(StatusCodes.NO_CONTENT, this)

    val <T> T.badRequest
        get() = ErrorHttpResponse<T, _, _>(StatusCodes.BAD_REQUEST, this)

    val <T> T.unauthorized
        get() = ErrorHttpResponse<T, _, _>(StatusCodes.UNAUTHORIZED, this)

    val <T> T.forbidden
        get() = ErrorHttpResponse<T, _, _>(StatusCodes.FORBIDDEN, this)

    val <T> T.notFound
        get() = ErrorHttpResponse<T, _, _>(StatusCodes.NOT_FOUND, this)

    val <T> T.serverError
        get() = ErrorHttpResponse<T, _, _>(StatusCodes.INTERNAL_SERVER_ERROR, this)
}