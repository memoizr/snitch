package me.snitchon.service

import me.snitchon.documentation.Visibility
import me.snitchon.parameters.HeaderParameter
import me.snitchon.parameters.Parameter
import me.snitchon.parameters.PathParam
import me.snitchon.parameters.QueryParameter
import me.snitchon.request.Body
import me.snitchon.request.ImplementationRequestWrapper
import me.snitchon.response.HttpResponse
import me.snitchon.types.HTTPMethods
import me.snitchon.types.StatusCodes

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
    val before: ImplementationRequestWrapper.() -> Unit = {},
    val after: ImplementationRequestWrapper.(HttpResponse<*, *>) -> Unit = {},
    val decorator: ImplementationRequestWrapper.(() -> HttpResponse<*, *>) -> HttpResponse<*, *> = { it() },
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


//    class FF(
//        wrap: ImplementationRequestWrapper, ): ImplementationRequestWrapper by wrap {
//        val next: () -> HttpResponse<*,*> =
//    }

    infix fun decorate(decoration: ImplementationRequestWrapper.(() -> HttpResponse<*, *>) -> HttpResponse<out Any?, StatusCodes>) = copy(decorator = decoration)

    infix fun doBefore(action: ImplementationRequestWrapper.() -> Unit) = copy(before = {
        before(this)
        action(this)
    })

    infix fun doAfter(action: ImplementationRequestWrapper.(HttpResponse<*, *>) -> Unit) = copy(after = {
        after(this, it)
        action(this, it)
    })
}
