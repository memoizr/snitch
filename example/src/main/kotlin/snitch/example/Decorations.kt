package snitch.example

import org.jetbrains.exposed.sql.transactions.transaction
import snitch.example.ApplicationModule.logger
import snitch.example.api.Headers.accessToken
import snitch.example.api.users.FORBIDDEN
import snitch.example.api.users.UNAUTHORIZED
import snitch.example.security.Authentication
import snitch.example.security.Role
import snitch.example.types.UserId
import snitch.parameters.Parameter
import snitch.request.RequestWrapper
import snitch.router.decorateWith
import snitch.service.ConditionResult.Failed
import snitch.service.ConditionResult.Successful
import snitch.service.condition

val logged = decorateWith {
    val method = method.name
    logger().info("Begin Request: $method $path")
    next().also {
        logger().info("End Request: $method $path ${it.statusCode.code} ${it.value(parser)}")
    }
}

val withTransaction get() = decorateWith { transaction { next() } }

val RequestWrapper.principal: UserId get() = (request[accessToken] as Authentication.Authenticated).claims.userId
val RequestWrapper.role: Role get() = (request[accessToken] as Authentication.Authenticated).claims.role

val authenticated = decorateWith(accessToken) {
    when (request[accessToken]) {
        is Authentication.Authenticated -> next()
        is Authentication.Unauthenticated -> UNAUTHORIZED()
    }
}

val hasAdminRole = condition("Admin role") {
    when (role) {
        Role.ADMIN -> Successful
        else -> Failed(FORBIDDEN())
    }
}

fun principalEquals(param: Parameter<out Any, *>) = condition("Principal equals ${param.name}") {
    if (principal.value == params(param.name)) Successful
    else Failed(FORBIDDEN())
}
