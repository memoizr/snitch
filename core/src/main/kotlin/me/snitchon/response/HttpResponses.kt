package me.snitchon.response

import me.snitchon.parsing.Parser
import me.snitchon.types.Format
import me.snitchon.types.StatusCodes
import me.snitchon.types.StatusCodes.BAD_REQUEST
import me.snitchon.types.StatusCodes.OK


context (Parser)
fun <T> HttpResponse<T>.format(newFormat: Format) =
    if (this is SuccessfulHttpResponse<T,*>) copy(_format = newFormat) else this

context (Parser)
fun <T> HttpResponse<T>.serializer(serializer: (T) -> Any) =
    if (this is SuccessfulHttpResponse<T,*>) copy(value = {serializer(this.body)}) else this

context(Parser)
inline val <reified T> T.ok: HttpResponse<T>
    get() = SuccessfulHttpResponse(StatusCodes.OK, this)

context(Parser)
inline val <reified T> T.created: HttpResponse<T>
    get() = SuccessfulHttpResponse(StatusCodes.CREATED, this)

context(Parser)
inline val <reified T> T.accepted: HttpResponse<T>
    get() = SuccessfulHttpResponse(StatusCodes.ACCEPTED, this)

context(Parser)
inline val <reified T> T.noContent: HttpResponse<T>
    get() = SuccessfulHttpResponse(StatusCodes.NO_CONTENT, this)

context(Parser)
inline val <reified T> T.badRequest: HttpResponse<T>
    get() = ErrorHttpResponse(StatusCodes.BAD_REQUEST, this)

context(Parser)
inline val <reified T> T.unauthorized: HttpResponse<T>
    get() = ErrorHttpResponse(StatusCodes.UNAUTHORIZED, this)

context(Parser)
inline val <reified T> T.forbidden: HttpResponse<T>
    get() = ErrorHttpResponse(StatusCodes.FORBIDDEN, this)

context(Parser)
inline val <reified T> T.notFound: HttpResponse<T>
    get() = ErrorHttpResponse(StatusCodes.NOT_FOUND, this)

context(Parser)
inline val <reified T> T.serverError: HttpResponse<T>
    get() = ErrorHttpResponse(StatusCodes.INTERNAL_SERVER_ERROR, this)
