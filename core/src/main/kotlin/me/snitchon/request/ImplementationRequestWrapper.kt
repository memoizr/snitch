package me.snitchon.request

import me.snitchon.parameters.*
import me.snitchon.parsing.Parser
import me.snitchon.response.CommonResponses
import me.snitchon.types.HTTPMethods
import me.snitchon.validation.UnregisteredParamException

interface ImplementationRequestWrapper : CommonResponses {
    val body: () -> Any?
    val params: Set<Parameter<*, *>>
    val parser: Parser

    fun method(): HTTPMethods

    fun params(name: String): String?
    fun headers(name: String): String?
    fun queryParams(name: String): String?
    fun getPathParam(param: PathParam<*, *>): String?
    fun getQueryParam(param: QueryParameter<*, *>): String?
    fun getHeaderParam(param: HeaderParameter<*, *>): String?

    private fun missingParameterMessage(path: String, it: Parameter<*, *>) =
        """Required $path parameter `${it.name}` is missing"""

    private fun invalidParameterMessage(query: String, it: Parameter<*, *>, value: String?) =
        """$query parameter `${it.name}` is invalid, expecting ${it.pattern.description}, got `$value`"""

    fun getInvalidParams(
        pathParams: Set<PathParam<out Any, *>>,
        queryParams: Set<QueryParameter<*, *>>,
        headerParams: Set<HeaderParameter<*, *>>,
    ): List<String> {
        return (pathParams.map { validateParam(it, getPathParam(it), "Path") } +
                queryParams.map { validateParam(it, getQueryParam(it), "Query") } +
                headerParams.map { validateParam(it, getHeaderParam(it), "Header") })
            .filterNotNull()
    }

    private fun validateParam(it: Parameter<*, *>, value: String?, path: String): String? {
        return when {
            it.required && value == null -> missingParameterMessage(path, it)
            !it.required && value == null -> null
            it.pattern.regex.matches(value.toString()) -> null
            else -> {
                invalidParameterMessage(path, it, value)
            }
        }
    }

    operator fun <T : Any, R> get(param: PathParam<T, R>): R =
        checkParamIsRegistered(param)
            .params(param.name)
            .let { param.pattern.parse(parser, it.orEmpty()) }

    operator fun <T : Any?, R> get(param: QueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .queryParams(param.name)
            .let { param.pattern.parse(parser, it.orEmpty()) }

    operator fun <T : Any?, R> get(param: OptionalQueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .queryParams(param.name)
            .filterValid(param)
            ?.let { param.pattern.parse(parser, it) } ?: param.default

    operator fun <T : Any?, R> get(param: HeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .headers(param.name)
            .let { param.pattern.parse(parser, it.orEmpty()) }

    operator fun <T : Any?, R> get(param: OptionalHeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .headers(param.name)
            .filterValid(param)
            ?.let { param.pattern.parse(parser, it) } ?: param.default

    private fun checkParamIsRegistered(param: Parameter<*, *>) =
        if (!params.contains(param)) throw UnregisteredParamException(param) else this
}

