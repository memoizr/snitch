package me.snitchon.service

import me.snitchon.documentation.Visibility
import me.snitchon.parameters.HeaderParameter
import me.snitchon.parameters.Parameter
import me.snitchon.parameters.PathParam
import me.snitchon.parameters.QueryParameter
import me.snitchon.request.Body
import me.snitchon.request.RequestWrapper
import me.snitchon.response.ErrorHttpResponse
import me.snitchon.response.HttpResponse
import me.snitchon.router.AfterAction
import me.snitchon.router.BeforeAction
import me.snitchon.types.HTTPMethods
import me.snitchon.types.StatusCodes

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
) {
    infix fun decorated(decoration: DecoratedWrapper.() -> HttpResponse<out Any, StatusCodes>): Endpoint<B> {
        val previousDecorator = this.decorator
        return copy(
            decorator = { DecoratedWrapper({ decoration(previousDecorator(this)) }, wrap) }
        )
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

    infix fun doBefore(action: BeforeAction) = decorated {
        action(wrap)
        next()
    }

    infix fun doAfter(action: AfterAction) = decorated {
        next().also { action(wrap, it) }
    }

    infix fun onlyIf(condition: Condition) = decorated {
        when (val result = condition.check(wrap)) {
            is ConditionResult.Successful -> next()
            is ConditionResult.Failed -> result.response
        }
    }
}

interface Condition {
    fun check(requestWrapper: RequestWrapper): ConditionResult
    infix fun or(other: Condition): Condition = OrCondition(this, other)
}

class OrCondition(private val first: Condition, private val second: Condition) : Condition {
    override fun check(requestWrapper: RequestWrapper) =
        when (val result = first.check(requestWrapper)) {
            is ConditionResult.Successful -> result
            is ConditionResult.Failed -> second.check(requestWrapper)
        }
}

sealed class ConditionResult {
    class Successful : ConditionResult()
    data class Failed(val response: ErrorHttpResponse<Any, out Any, StatusCodes.BAD_REQUEST>) : ConditionResult()
}
