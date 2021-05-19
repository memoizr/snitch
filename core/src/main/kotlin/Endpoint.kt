package com.snitch

import com.snitch.documentation.Visibility

data class OpDescription(val description: String)

data class Endpoint<B : Any>(
    val httpMethod: HTTPMethod,
    val summary: String?,
    val description: String?,
    val url: String,
    val pathParams: Set<PathParam<out Any, *>>,
    val queryParams: Set<QueryParameter<*, *>>,
    val headerParams: Set<HeaderParameter<*, *>>,
    val body: Body<B>,
    val tags: List<String>? = emptyList(),
    val visibility: Visibility = Visibility.PUBLIC,
    val before: (RequestWrapper) -> Unit = {},
    val after: (RequestWrapper, ResponseWrapper) -> Unit = { _, res -> res},
) {

    infix fun withQuery(queryParameter: QueryParameter<*, *>) = copy(queryParams = queryParams + queryParameter)
    infix fun withHeader(params: HeaderParameter<*, *>) = copy(headerParams = headerParams + params)
    infix fun <C : Any> with(body: Body<C>) = Endpoint(
        httpMethod,
        summary,
        description,
        url,
        pathParams,
        queryParams,
        headerParams,
        body,
        tags,
        visibility,
        before,
        after,
    )

    infix fun inSummary(summary: String) = copy(summary = summary)
    infix fun isDescribedAs(description: String) = copy(description = description)
    infix fun with(visibility: Visibility) = copy(visibility = visibility)
    infix fun with(queryParameter: List<Parameter<*, *>>) = let {
        queryParameter.foldRight(this) { param, endpoint ->
            when (param) {
                is HeaderParameter -> endpoint withHeader param
                is QueryParameter -> endpoint withQuery param
                else -> throw IllegalArgumentException(param.toString())
            }
        }
    }
}

interface RequestWrapper {
    val body: String
    fun params(name: String): String?
    fun headers(name: String): String?
    fun queryParams(name: String): String?

    fun method(): HTTPMethod


    fun getPathParam(param: PathParam<*, *>): String?
    fun getQueryParam(param: QueryParameter<*, *>): String?
    fun getHeaderParam(param: HeaderParameter<*, *>): String?


    fun missingParameterMessage(path: String, it: Parameter<*, *>) =
        """Required $path parameter `${it.name}` is missing"""

    fun invalidParameterMessage(query: String, it: Parameter<*, *>, value: String?) =
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

    fun validateParam(it: Parameter<*, *>, value: String?, path: String): String? {
        return when {
            it.required && value == null -> missingParameterMessage(path, it)
            !it.required && value == null -> null
            it.pattern.regex.matches(value.toString()) -> null
            else -> {
                invalidParameterMessage(path, it, value)
            }
        }
    }
}

interface ResponseWrapper {
    fun setStatus(code: Int)
    fun setType(type: Format)
}
