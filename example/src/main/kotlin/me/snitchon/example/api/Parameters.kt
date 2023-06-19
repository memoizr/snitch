package me.snitchon.example.api

import com.snitch.me.snitchon.NonEmptyString
import me.snitchon.example.api.validation.ValidAccessToken
import me.snitchon.parameters.header
import me.snitchon.parameters.path

object Paths {
    val userId = path("userId", condition = NonEmptyString)
    val postId = path("postId", condition = NonEmptyString)
}

object Headers {
    val accessToken = header("X-Access-Token",
        condition = ValidAccessToken,
        description = "Access token for the principal user")
    val accessTokens = header("X-Access-Tokens",
        condition = ValidAccessToken,
        description = "Access token for the principal user")
}
