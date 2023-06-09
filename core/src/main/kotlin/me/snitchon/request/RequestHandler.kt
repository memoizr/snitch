package me.snitchon.request

import me.snitchon.response.HttpResponse
import me.snitchon.service.OpDescription
import me.snitchon.ResponseWrapper
import me.snitchon.documentation.ContentType
import me.snitchon.parameters.*
import me.snitchon.parsing.Parser
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KType

data class RequestHandler<T : Any>
    (
    private val _body: Body<T>?,
    val params: Set<Parameter<*, *>>,
    val request: RequestWrapper,
    val response: ResponseWrapper
) {

    val body: T by lazy { request.body() as T }

    fun RequestWrapper.checkParamIsRegistered(param: Parameter<*, *>) =
        if (!params.contains(param)) throw UnregisteredParamException(param) else this

    inline operator fun <reified T : Any, R> RequestWrapper.get(param: PathParam<T, R>): R =
        checkParamIsRegistered(param)
            .params(param.name)
            .let { param.pattern.parse(it.orEmpty()) }

    inline operator fun <reified T : Any?, R> RequestWrapper.get(param: QueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .queryParams(param.name)
            .let { param.pattern.parse(it.orEmpty()) }

    inline operator fun <reified T : Any?, R> RequestWrapper.get(param: OptionalQueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .queryParams(param.name)
            .filterValid(param)
            ?.let { param.pattern.parse(it) } ?: param.default

    inline operator fun <reified T : Any?, R> RequestWrapper.get(param: HeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .headers(param.name)
            .let { param.pattern.parse(it.orEmpty()) }

    inline operator fun <reified T : Any?, R> RequestWrapper.get(param: OptionalHeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .headers(param.name)
            .filterValid(param)
            ?.let { param.pattern.parse(it) } ?: param.default
}

inline operator fun <reified T : Any, R> RequestWrapper.get(param: PathParam<T, R>): R =
    params(param.name)
        .let { param.pattern.parse(it.orEmpty()) }

inline operator fun <reified T : Any?, R> RequestWrapper.get(param: QueryParam<T, R>): R =
    queryParams(param.name)
        .let { param.pattern.parse(it.orEmpty()) }

inline operator fun <reified T : Any?, R> RequestWrapper.get(param: OptionalQueryParam<T, R>): R =
    queryParams(param.name)
        .filterValid(param)
        ?.let { param.pattern.parse(it) } ?: param.default

inline operator fun <reified T : Any?, R> RequestWrapper.get(param: HeaderParam<T, R>): R =
    headers(param.name)
        .let { param.pattern.parse(it.orEmpty()) }

inline operator fun <reified T : Any?, R> RequestWrapper.get(param: OptionalHeaderParam<T, R>): R =
    headers(param.name)
        .filterValid(param)
        ?.let { param.pattern.parse(it) } ?: param.default


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

class Handler<Request : Any, Response>(val block: context(Parser) RequestHandler<Request>.() -> HttpResponse<Response>) {
    operator fun getValue(
        nothing: Nothing?,
        property: KProperty<*>
    ): Pair<KType, context(Parser) RequestHandler<Request>.() -> HttpResponse<Response>> {
        val type = property.returnType.arguments[1].type!!.arguments[2].type!!.arguments[0].type!!
        return type to block
    }
}
