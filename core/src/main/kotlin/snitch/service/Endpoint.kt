package snitch.service

import snitch.documentation.Visibility
import snitch.parameters.HeaderParameter
import snitch.parameters.Parameter
import snitch.parameters.PathParam
import snitch.parameters.QueryParameter
import snitch.request.Body
import snitch.request.RequestWrapper
import snitch.response.ErrorHttpResponse
import snitch.response.HttpResponse
import snitch.router.AfterAction
import snitch.router.BeforeAction
import snitch.types.HTTPMethods
import snitch.types.StatusCodes

data class OpDescription(val description: String)

data class Endpoint<B : Any>(
    val httpMethod: HTTPMethods,
    val summary: String?,
    val description: String?,
    val path: String,
    val pathParams: Set<PathParam<out Any, *>>,
    val queryParams: Set<QueryParameter<*, *>>,
    val headerParams: Set<HeaderParameter<*, *>>,
    val body: Body<B>,
    val tags: List<String>? = emptyList(),
    val visibility: Visibility = Visibility.PUBLIC,
    val decorator: DecoratedWrapper.() -> DecoratedWrapper = { this },
    val conditions: List<Condition> = emptyList(),
    val security: String? = null
) {

    infix fun decorated(decoration: DecoratedWrapper.() -> HttpResponse<out Any, StatusCodes>): Endpoint<B> {
        val previousDecorator = this.decorator
        return copy(
            decorator = { DecoratedWrapper({ decoration(previousDecorator(this)) }, wrap) }
        )
    }

    infix fun decoratedPost(decoration: DecoratedWrapper.() -> HttpResponse<out Any, StatusCodes>): Endpoint<B> {
        val previousDecorator = this.decorator
        return copy(decorator = { previousDecorator(DecoratedWrapper({ decoration(this@copy) }, wrap)) })
    }

    infix fun withQuery(queryParameter: QueryParameter<*, *>) = copy(queryParams = queryParams + queryParameter)

    infix fun withHeader(params: HeaderParameter<*, *>) = copy(headerParams = headerParams + params)
    infix fun <C : Any> with(body: Body<C>) = Endpoint(
        httpMethod,
        summary,
        description,
        path,
        pathParams,
        queryParams,
        headerParams,
        body,
        tags,
        visibility,
        decorator,
        conditions,
        security,
    )

    infix fun inSummary(summary: String) = copy(summary = summary)

    fun applyConditions(): Endpoint<B> =
        copy(
            summary = (summary ?: "")
                    + if (conditions.isNotEmpty()) " "
                    + conditions.map { it.description }.joinToString("") else "",
        ).let {
            conditions.foldRight(it) { condition, endpoint ->
                condition.transform(endpoint) as Endpoint<B>
            }
        }.let {
            if (it.conditions.isNotEmpty()) {
                it.decoratedPost {
                    val condition = conditions.reduce { condition, next ->
                        condition and next
                    }
                    when (val result = condition.check(wrap)) {
                        is ConditionResult.Successful -> next()
                        is ConditionResult.Failed -> result.response
                    }
                }
            } else {
                it
            }
        }

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

    infix fun doBefore(action: BeforeAction) = decorated {
        action(wrap)
        next()
    }

    infix fun doAfter(action: AfterAction) = decorated {
        next().also { action(wrap, it) }
    }

    infix fun onlyIf(condition: Condition) = copy(conditions = conditions + condition)
}

