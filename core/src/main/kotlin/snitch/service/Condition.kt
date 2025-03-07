package snitch.service

import snitch.request.RequestWrapper
import snitch.response.ErrorHttpResponse
import snitch.types.StatusCodes

interface Condition {
    val transform: Endpoint<*>.() -> Endpoint<*>
    val description: String
    fun check(requestWrapper: RequestWrapper): ConditionResult
    infix fun or(other: Condition): Condition = OrCondition(this, other)
    infix fun and(other: Condition): Condition = AndCondition(this, other)
    operator fun not(): Condition = NotCondition(this)
}

class OrCondition(private val first: Condition, private val second: Condition) : Condition {
    override val transform: Endpoint<*>.() -> Endpoint<*>
        get() = { first.transform(second.transform(this)) }
    override val description: String
        get() = "(${first.description} or ${second.description})"

    override fun check(requestWrapper: RequestWrapper) =
        when (val result = first.check(requestWrapper)) {
            is ConditionResult.Successful -> result
            is ConditionResult.Failed -> second.check(requestWrapper)
        }
}

class AndCondition(private val first: Condition, private val second: Condition) : Condition {
    override val transform: Endpoint<*>.() -> Endpoint<*>
        get() = { first.transform(second.transform(this)) }

    override val description: String
        get() = "(${first.description} and ${second.description})"

    override fun check(requestWrapper: RequestWrapper) =
        when (val result = first.check(requestWrapper)) {
            is ConditionResult.Successful -> second.check(requestWrapper)
            is ConditionResult.Failed -> result
        }
}

class NotCondition(private val condition: Condition) : Condition {
    override val transform: Endpoint<*>.() -> Endpoint<*>
        get() = condition.transform

    override val description: String
        get() = "not ${condition.description}"

    override fun check(requestWrapper: RequestWrapper): ConditionResult =
        when (condition.check(requestWrapper)) {
            is ConditionResult.Successful -> ConditionResult.Failed(
                ErrorHttpResponse(StatusCodes.BAD_REQUEST, "condition: ${condition.description} negated")
            )
            is ConditionResult.Failed -> ConditionResult.Successful
        }
}

sealed class ConditionResult {
    data object Successful : ConditionResult()
    data class Failed(val response: ErrorHttpResponse<Any, out Any, StatusCodes.BAD_REQUEST>) : ConditionResult()
}

fun condition(
    description: String,
    transform: Endpoint<*>.() -> Endpoint<*> = { this },
    cond: RequestWrapper.() -> ConditionResult,
) = object : Condition {
    override val transform: Endpoint<*>.() -> Endpoint<*> = transform
    override val description: String = description
    override fun check(requestWrapper: RequestWrapper): ConditionResult {
        return cond(requestWrapper)
    }
}
