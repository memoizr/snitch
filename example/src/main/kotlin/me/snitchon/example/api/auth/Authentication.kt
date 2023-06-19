package me.snitchon.example.api.auth

import io.jsonwebtoken.JwtException
import me.snitchon.router.Router
import me.snitchon.example.api.Headers.accessToken
import me.snitchon.example.security.Authentication
import me.snitchon.example.types.ForbiddenException
import me.snitchon.example.types.UserId
import me.snitchon.extensions.print
import me.snitchon.parameters.InvalidParametersException
import me.snitchon.parameters.PathParam
import me.snitchon.request.Context
import me.snitchon.request.RequestWrapper
import me.snitchon.router.Routes
import me.snitchon.router.decorateAll
import me.snitchon.router.using
import me.snitchon.service.Endpoint
import me.snitchon.types.ErrorResponse
import me.snitchon.types.StatusCodes
import me.snitchon.types.StatusCodes.UNAUTHORIZED

//fun <B : Any> Endpoint<B>.authenticated(): Endpoint<B> = copy(
//    headerParams = headerParams + accessToken
//).doBefore {
//    when (this[accessToken]) {
//        is Authentication.Authenticated ->
//        is Authentication.Unauthenticated -> TODO()
//    }
//}


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

//fun Router.authenticated(routes: Routes) = applyToAll({
//    routes()
//}) { authenticated() }

fun Router.withPrincipalMatchingParameter(pathParam: PathParam<out Any, *>, routes: Routes) = applyToAll({
    routes()
}) { principalOf(pathParam) }

infix fun <B : Any> Endpoint<B>.principalOf(pathParam: PathParam<out Any, *>): Endpoint<B> = copy(
    pathParams = pathParams.map {
        if (it.name == pathParam.name) {
            it.copy(description = it.description + " " + "needs to match the principal")
        } else it
    }.toSet()).decorate {
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
