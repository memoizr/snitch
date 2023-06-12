package me.snitchon.request

import me.snitchon.response.HttpResponse
import me.snitchon.service.OpDescription
import me.snitchon.types.ContentType
import me.snitchon.extensions.print
import me.snitchon.parameters.*
import me.snitchon.parsing.Parser
import me.snitchon.response.ErrorHttpResponse
import me.snitchon.response.SuccessfulHttpResponse
import me.snitchon.types.Format
import me.snitchon.types.StatusCodes
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType

@JvmInline
value class RequestHandler<T : Any>(
    val request: RequestWrapper,
) : CommonResponses {

    val body: T get () = request.body() as T

    fun RequestWrapper.checkParamIsRegistered(param: Parameter<*, *>) =
        if (!params.contains(param)) throw UnregisteredParamException(param) else this

    inline operator fun <reified T : Any, R> RequestWrapper.get(param: PathParam<T, R>): R =
        checkParamIsRegistered(param)
            .params(param.name)
            .let { param.pattern.parse(parser, it.orEmpty()) }

    inline operator fun <reified T : Any?, R> RequestWrapper.get(param: QueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .queryParams(param.name)
            .let { param.pattern.parse(parser, it.orEmpty()) }

    inline operator fun <reified T : Any?, R> RequestWrapper.get(param: OptionalQueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .queryParams(param.name)
            .filterValid(param)
            ?.let { param.pattern.parse(parser, it) } ?: param.default

    inline operator fun <reified T : Any?, R> RequestWrapper.get(param: HeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .headers(param.name)
            .let { param.pattern.parse(parser, it.orEmpty()) }

    inline operator fun <reified T : Any?, R> RequestWrapper.get(param: OptionalHeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .headers(param.name)
            .filterValid(param)
            ?.let { param.pattern.parse(parser, it) } ?: param.default
}


fun queries(vararg queryParameter: QueryParameter<*, *>) = queryParameter.asList()
fun headers(vararg headerParameter: HeaderParameter<*, *>) = headerParameter.asList()
fun description(description: String) = OpDescription(description)
inline fun <reified T : Any> body(contentType: ContentType = ContentType.APPLICATION_JSON) = Body(T::class, contentType)

fun String?.filterValid(param: Parameter<*, *>): String? = when {
    this == null -> null
    param.emptyAsMissing && this.isEmpty() -> null
    param.invalidAsMissing && !param.pattern.regex.matches(this) -> null
    else -> this
}

data class Body<T : Any>(val klass: KClass<T>, val contentType: ContentType = ContentType.APPLICATION_JSON)

data class UnregisteredParamException(val param: Parameter<*, *>) : Exception()

class Handler<Request : Any, Response, S : StatusCodes>(val block: context(Parser) RequestHandler<Request>.() -> HttpResponse<Response, S>) {
    operator fun getValue(
        nothing: Nothing?,
        property: KProperty<*>
    ): HandlerResponse<Request, Response, S> {
        val type = property.returnType.arguments.get(1).type.print()
        val statusCode = property.returnType.arguments.get(2).type

        return HandlerResponse(statusCode!!, type!!, block)
    }
}

data class HandlerResponse<Request : Any, Response, S : StatusCodes>(
    val statusCodes: KType,
    val type: KType,
    val handler:
    context(Parser) RequestHandler<Request>.() -> HttpResponse<Response, S>
)

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