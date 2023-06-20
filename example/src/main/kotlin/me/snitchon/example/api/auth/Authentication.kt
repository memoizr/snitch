package me.snitchon.example.api.auth

import me.snitchon.example.api.Headers.accessToken
import me.snitchon.example.api.users.FORBIDDEN
import me.snitchon.example.api.users.UNAUTHORIZED
import me.snitchon.example.security.Authentication
import me.snitchon.example.security.Role
import me.snitchon.example.security.Role.ADMIN
import me.snitchon.example.types.UserId
import me.snitchon.parameters.Parameter
import me.snitchon.request.Context
import me.snitchon.request.RequestWrapper
import me.snitchon.router.Router
import me.snitchon.router.decorateAll
import me.snitchon.service.Condition
import me.snitchon.service.ConditionResult
import me.snitchon.service.ConditionResult.Failed
import me.snitchon.service.ConditionResult.Successful


val <T : Any> Context<T>.principal: UserId get() = (request[accessToken] as Authentication.Authenticated).claims.userId
val RequestWrapper.principal: UserId get() = (request[accessToken] as Authentication.Authenticated).claims.userId
val RequestWrapper.role: Role get() = (request[accessToken] as Authentication.Authenticated).claims.role

val Router.authenticated
    get() = decorateAll {
        with(listOf(accessToken)).decorate {
            when (request[accessToken]) {
                is Authentication.Authenticated -> next()
                is Authentication.Unauthenticated -> UNAUTHORIZED()
            }
        }
    }

fun condition(cond: RequestWrapper.() -> ConditionResult) = object : Condition {
    override fun check(requestWrapper: RequestWrapper): ConditionResult {
        return cond(requestWrapper)
    }
}

val hasAdminRole = condition {
    when (role) {
        ADMIN -> Successful()
        else -> Failed(FORBIDDEN())
    }
}

fun principalEquals(param: Parameter<out Any, *>) = condition {
    if (principal.value == params(param.name)) Successful()
    else Failed(FORBIDDEN())
}

