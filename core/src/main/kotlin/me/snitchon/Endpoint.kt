package me.snitchon

import me.snitchon.documentation.ContentType
import me.snitchon.documentation.Visibility

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
