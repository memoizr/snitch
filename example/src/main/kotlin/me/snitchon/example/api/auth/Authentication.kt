package me.snitchon.example.api.auth

import io.jsonwebtoken.JwtException
import me.snitchon.example.api.Headers.accessToken
import me.snitchon.example.security.verifyJWT
import me.snitchon.example.types.ForbiddenException
import me.snitchon.example.types.UserId
import me.snitchon.parameters.InvalidParametersException
import me.snitchon.parameters.PathParam
import me.snitchon.request.Context
import me.snitchon.service.Endpoint

fun <B : Any> Endpoint<B>.authenticated(): Endpoint<B> = copy(
    headerParams = headerParams + accessToken,
    before = {
        it.headers(accessToken.name)
            ?.let { verifyJWT(it) }
    }
)

fun <B : Any> Endpoint<B>.principalMatches(pathParam: PathParam<out Any, *>): Endpoint<B> = copy(
    pathParams = pathParams.map {
        if (it.name == pathParam.name) {
            it.copy(description = it.description + " " + "needs to match the principal")
        }
        else it
    }.toSet(),
    before = {
        try {
            if (it.params(pathParam.name) != it.get(accessToken).value)
                throw ForbiddenException()
        } catch (e: InvalidParametersException) {
            if (e.e is JwtException)
                throw e.e
            else throw e
        }
    }
)

val <T : Any> Context<T>.principal: UserId get() = request[accessToken]
