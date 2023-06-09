package me.snitchon.response

import me.snitchon.parsing.Parser
import me.snitchon.types.Format
import me.snitchon.types.StatusCodes


context (Parser)
inline fun <T, reified S: StatusCodes> HttpResponse<T, S>.format(newFormat: Format) =
    if (this is SuccessfulHttpResponse<T,S>) copy(_format = newFormat) else this

context (Parser)
inline fun <T, reified S: StatusCodes> HttpResponse<T, S>.serializer(noinline serializer: (T) -> Any) =
    if (this is SuccessfulHttpResponse<T,S>) copy(value = {serializer(this.body)}) else this

context(Parser)
inline val <reified T> T.ok
    get() = SuccessfulHttpResponse(StatusCodes.OK, this)

context(Parser)
inline val <reified T> T.created
    get() = SuccessfulHttpResponse(StatusCodes.CREATED, this)

context(Parser)
inline val <reified T> T.accepted
    get() = SuccessfulHttpResponse(StatusCodes.ACCEPTED, this)

context(Parser)
inline val <reified T> T.noContent
    get() = SuccessfulHttpResponse(StatusCodes.NO_CONTENT, this)

context(Parser)
inline val <reified T> T.badRequest
    get() = ErrorHttpResponse<T,_,_>(StatusCodes.BAD_REQUEST, this)

context(Parser)
inline val <reified T> T.unauthorized
    get() = ErrorHttpResponse<T,_,_>(StatusCodes.UNAUTHORIZED, this)

context(Parser)
inline val <reified T> T.forbidden
    get() = ErrorHttpResponse<T,_,_>(StatusCodes.FORBIDDEN, this)

context(Parser)
inline val <reified T> T.notFound
    get() = ErrorHttpResponse<T,_,_>(StatusCodes.NOT_FOUND, this)

context(Parser)
inline val <reified T> T.serverError
    get() = ErrorHttpResponse<T,_,_>(StatusCodes.INTERNAL_SERVER_ERROR, this)
