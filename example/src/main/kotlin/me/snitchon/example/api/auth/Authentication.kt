package me.snitchon.example.api.auth

import io.jsonwebtoken.JwtException
import me.snitchon.example.api.Headers
import me.snitchon.router.Router
import me.snitchon.example.api.Headers.accessToken
import me.snitchon.example.api.users._403
import me.snitchon.example.security.Authentication
import me.snitchon.example.security.Role
import me.snitchon.example.security.Role.*
import me.snitchon.example.types.ForbiddenException
import me.snitchon.example.types.UserId
import me.snitchon.extensions.print
import me.snitchon.parameters.InvalidParametersException
import me.snitchon.parameters.Parameter
import me.snitchon.parameters.PathParam
import me.snitchon.request.Context
import me.snitchon.request.RequestWrapper
import me.snitchon.router.decorateAll
import me.snitchon.service.Condition
import me.snitchon.service.ConditionResult
import me.snitchon.service.ConditionResult.Failed
import me.snitchon.service.ConditionResult.Successful
import me.snitchon.service.Endpoint
import me.snitchon.types.ErrorResponse
import me.snitchon.types.StatusCodes.UNAUTHORIZED

val Router.authenticated
    get() = decorateAll {
        with(listOf(accessToken)).decorate {
            when (this[accessToken]) {
                is Authentication.Authenticated -> next()
                is Authentication.Unauthenticated -> ErrorResponse(
                    401,
                    "unauthorized"
                ).unauthorized<Any, _, UNAUTHORIZED>()
            }
        }
    }

infix fun <B : Any> Endpoint<B>.principalOf(pathParam: PathParam<out Any, *>): Endpoint<B> = copy(
    pathParams = pathParams.map {
        if (it.name == pathParam.name) {
            it.copy(description = it.description + " " + "needs to match the principal")
        } else it
    }.toSet()
).decorate {
    try {
        if (this.params(pathParam.name).print() != principal.value.print())
            throw ForbiddenException()
        else next()
    } catch (e: InvalidParametersException) {
        if (e.e is JwtException)
            throw e.e
        else throw e
    }
}

val <T : Any> Context<T>.principal: UserId get() = (request[accessToken] as Authentication.Authenticated).claims.userId
val RequestWrapper.principal: UserId get() = (request[accessToken] as Authentication.Authenticated).claims.userId

val RequestWrapper.role: Role get() = (request[accessToken] as Authentication.Authenticated).claims.role

fun condition(cond: RequestWrapper.() -> ConditionResult) = object : Condition {
    override fun check(requestWrapper: RequestWrapper): ConditionResult {
        return cond(requestWrapper)
    }
}

val hasAdminRole = condition {
    when (role) {
        ADMIN -> Successful()
        else -> Failed(_403())
    }
}

fun principalMatches(param: Parameter<out Any, *>) = condition {
    if (principal.value == params(param.name)) Successful()
    else Failed(_403())
}

