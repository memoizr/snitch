package me.snitchon.example.api.auth

import me.snitchon.example.api.Headers.accessToken
import me.snitchon.example.security.verifyJWT
import me.snitchon.example.types.UserId
import me.snitchon.request.Context
import me.snitchon.service.Endpoint

fun <B : Any> Endpoint<B>.authenticated(): Endpoint<B> = copy(
    headerParams = headerParams + accessToken,
    before = {
        it.headers(accessToken.name)
            ?.let { verifyJWT(it) }
    }
)

val <T: Any> Context<T>.principal: UserId get() = request[accessToken]
