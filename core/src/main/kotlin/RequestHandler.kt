package com.snitch

import com.beust.klaxon.Klaxon
import com.snitch.extensions.klaxon
import com.snitch.extensions.parse
import kotlin.reflect.KClass

data class RequestHandler<T : Any>
(
        private val _body: Body<T>?,
        val params: Set<Parameter<*,*>>,
        val request: RequestWrapper,
        val response: ResponseWrapper) {

    val body: T by lazy { _body?.customGson?.parse(
        request.body
        , _body.klass)!! }

    fun RequestWrapper.checkParamIsRegistered(param: Parameter<*,*>) = if (!params.contains(param)) throw UnregisteredParamException(param) else this

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

    inline operator fun <reified T : Any?, R> RequestWrapper.get(param: OptionalHeaderParam<T,R>): R =
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

inline operator fun <reified T : Any?, R> RequestWrapper.get(param: OptionalHeaderParam<T,R>): R =
        headers(param.name)
        .filterValid(param)
        ?.let { param.pattern.parse(it) } ?: param.default


fun queries(vararg queryParameter: QueryParameter<*,*>) = queryParameter.asList()
fun headers(vararg headerParameter: HeaderParameter<*,*>) = headerParameter.asList()
fun description(description: String) = OpDescription(description)
inline fun <reified T : Any> body(customGson: Klaxon = klaxon) = Body(T::class, customGson)

fun String?.filterValid(param: Parameter<*,*>): String? = when {
    this == null -> null
    param.emptyAsMissing && this.isEmpty() -> null
    param.invalidAsMissing && !param.pattern.regex.matches(this) -> null
    else -> this
}

data class Body<T : Any>(val klass: KClass<T>, val customGson: Klaxon = klaxon)

data class UnregisteredParamException(val param: Parameter<*,*>) : Throwable()

typealias Handler<BODY_TYPE, RESPONSE_TYPE> =
        RequestHandler<BODY_TYPE>.() -> HttpResponse<RESPONSE_TYPE>
