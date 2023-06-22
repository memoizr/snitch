package me.snitchon.example.api

import me.snitchon.example.api.validation.validAccessToken
import me.snitchon.parameters.header
import me.snitchon.parameters.path

object Paths {
    val userId by path()
    val postId by path()
}

object Headers {
    val accessToken by header(
        condition = validAccessToken,
        "X-Access-Token",
        description = "Access token for the principal user"
    )
}
