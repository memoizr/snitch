package snitch.auth

import snitch.auth.ValidationHeaders.accessToken
import snitch.parameters.Parameter
import snitch.request.RequestWrapper
import snitch.router.decorateWith
import snitch.service.ConditionResult.Failed
import snitch.service.ConditionResult.Successful
import snitch.service.condition
import snitch.types.ErrorResponse
import snitch.types.StatusCodes
import java.util.*

val RequestWrapper.principal: String get() = (request[accessToken] as Authentication.Authenticated).claims.userId
val RequestWrapper.role: Role get() = (request[accessToken] as Authentication.Authenticated).claims.role

val authenticated = decorateWith(accessToken) {
    when (request[accessToken]) {
        is Authentication.Authenticated -> next()
        is Authentication.Unauthenticated -> UNAUTHORIZED()
    }
}

fun principalEquals(param: Parameter<out Any, *>) = condition("Principal equals ${param.name}") {
    if (principal == params(param.name)) Successful
    else Failed(FORBIDDEN())
}

fun <T, S : StatusCodes> RequestWrapper.FORBIDDEN() =
    ErrorResponse(403, "forbidden").forbidden<T, _, S>()

fun <T, S : StatusCodes> RequestWrapper.UNAUTHORIZED() =
    ErrorResponse(401, "unauthorized").unauthorized<T, _, S>()
