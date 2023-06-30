package snitch.request

import snitch.extensions.print
import snitch.parameters.*
import snitch.parsing.Parser
import snitch.response.CommonResponses
import snitch.types.HTTPMethods
import snitch.validation.UnregisteredParamException

interface RequestWrapper : CommonResponses {
    val body: () -> Any?
    val params: Set<Parameter<*, *>>
    val parser: Parser
    val method: HTTPMethods
    val path: String
    val request get() = this

    fun params(name: String): String?
    fun headers(name: String): Collection<String>
    fun queryParams(name: String): Collection<String>
    fun getPathParam(param: PathParam<*, *>): String?
    fun getQueryParam(param: QueryParameter<*, *>): Collection<String>?
    fun getHeaderParam(param: HeaderParameter<*, *>): Collection<String>?

    private fun missingParameterMessage(path: String, it: Parameter<*, *>) =
        """Required $path parameter `${it.name}` is missing"""

    private fun invalidParameterMessage(query: String, it: Parameter<*, *>, value: Collection<String>?) =
        """$query parameter `${it.name}` is invalid, expecting ${it.pattern.description}, got `${value?.joinToString(",")}`"""

    fun getInvalidParams(): List<String> {
        return params
            .map {
                when (it) {
                    is PathParam<*, *> -> validateParam(it, listOf(getPathParam(it).orEmpty()), "Path")
                    is QueryParameter<*, *> -> validateParam(it, getQueryParam(it), "Query")
                    is HeaderParameter<*, *> -> validateParam(it, getHeaderParam(it), "Header")
                }
            }.filterNotNull().print()
    }

    private fun validateParam(it: Parameter<*, *>, value: Collection<String>?, path: String): String? {
        return try {
            when {
                it.required && (value == null || value.isEmpty()) -> missingParameterMessage(path, it)
                !it.required && value == null -> null
                value != null && it.pattern.parse(parser, value).let { true } -> null
                it.pattern.regex.matches(value.toString()) -> null
                else -> invalidParameterMessage(path, it, value)

            }
        } catch (e: Exception) {
            invalidParameterMessage(path, it, value)
        }
    }

    operator fun <T : Any, R> get(param: PathParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam(param) {
                params(param.name)
                    .let { param.pattern.parse(parser, listOf(it.orEmpty())) }
            }!!

    operator fun <T : Any?, R> get(param: QueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam(param) {
                queryParams(param.name)
                    .filterValid(param)
                    .let { param.pattern.parse(parser, it.orEmpty()) }
            }!!

    operator fun <T : Any?, R> get(param: OptionalQueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam(param) {
                queryParams(param.name)
                    .filterValid(param).print()
                    ?.let { param.pattern.parse(parser, it).print() }.print()
            } ?: param.default

    operator fun <T : Any?, R> get(param: HeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam(param) {
                headers(param.name)
                    .let { param.pattern.parse(parser, it) }
            }!!


    operator fun <T : Any?, R> get(param: OptionalHeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam(param) {
                headers(param.name)
                    .filterValid(param)
                    ?.let { param.pattern.parse(parser, it) }
            } ?: param.default
}

private inline fun RequestWrapper.checkParamIsRegistered(param: Parameter<*, *>) =
    if (!params.contains(param)) throw UnregisteredParamException(param) else this

private inline fun <R> RequestWrapper.tryParam(param: Parameter<*, R>, block: () -> R?): R? = try {
    block()
} catch (e: Exception) {
    if (param is OptionalParam<*>&& param.invalidAsMissing) {
        null
    } else throw InvalidParametersException(e, getInvalidParams())
}

fun Collection<String>.filterValid(param: Parameter<*, *>): Collection<String>? = when {
    this.isEmpty() -> null
    param.emptyAsMissing && this.all { it.isEmpty() } -> null
    else -> this
}

fun String?.filterValid(param: Parameter<*, *>): String? = when {
    this == null -> null
    param.emptyAsMissing && this.isEmpty() -> null
    else -> this
}
