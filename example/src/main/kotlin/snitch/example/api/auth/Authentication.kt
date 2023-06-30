package snitch.example.api.auth

import snitch.parameters.Parameter
import snitch.request.RequestWrapper
import snitch.router.Router
import snitch.router.transformEndpoints
import snitch.service.Condition
import snitch.service.ConditionResult
import snitch.service.ConditionResult.Failed
import snitch.service.ConditionResult.Successful
import snitch.example.api.Headers.accessToken
import snitch.example.api.users.FORBIDDEN
import snitch.example.api.users.UNAUTHORIZED
import snitch.example.security.Authentication
import snitch.example.security.Role
import snitch.example.types.UserId


//val <T : Any> TypedRequestWrapper<T>.principal: UserId get() = (request[accessToken] as Authentication.Authenticated).claims.userId
val RequestWrapper.principal: UserId get() = (request[accessToken] as Authentication.Authenticated).claims.userId
val RequestWrapper.role: Role get() = (request[accessToken] as Authentication.Authenticated).claims.role

val Router.authenticated
    get() = transformEndpoints {
        with(listOf(accessToken)).decorated {
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
        Role.ADMIN -> Successful()
        else -> Failed(FORBIDDEN())
    }
}

fun principalEquals(param: Parameter<out Any, *>) = condition {
    if (principal.value == params(param.name)) Successful()
    else Failed(FORBIDDEN())
}

