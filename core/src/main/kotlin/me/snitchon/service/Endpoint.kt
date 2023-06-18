package me.snitchon.service

import me.snitchon.documentation.Visibility
import me.snitchon.parameters.HeaderParameter
import me.snitchon.parameters.Parameter
import me.snitchon.parameters.PathParam
import me.snitchon.parameters.QueryParameter
import me.snitchon.request.Body
import me.snitchon.router.AfterAction
import me.snitchon.router.BeforeAction
import me.snitchon.router.Decoration
import me.snitchon.types.HTTPMethods

data class OpDescription(val description: String)

data class Endpoint<B : Any>(
    val httpMethod: HTTPMethods,
    val summary: String?,
    val description: String?,
    val url: String,
    val pathParams: Set<PathParam<out Any, *>>,
    val queryParams: Set<QueryParameter<*, *>>,
    val headerParams: Set<HeaderParameter<*, *>>,
    val body: Body<B>,
    val tags: List<String>? = emptyList(),
    val visibility: Visibility = Visibility.PUBLIC,
    val before: BeforeAction = {},
    val after: AfterAction = {},
    val decorator: Decoration = { next() },
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

    infix fun decorate(decoration: Decoration) = copy(decorator = decoration)

    infix fun doBefore(action: BeforeAction) = copy(before = {
        before(this)
        action(this)
    })

    infix fun doAfter(action: AfterAction) = copy(after = {
        after(this, it)
        action(this, it)
    })
}
