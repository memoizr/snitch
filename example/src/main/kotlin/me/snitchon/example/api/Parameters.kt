package me.snitchon.example.api

import com.snitch.me.snitchon.NonEmptyString
import me.snitchon.example.api.validation.validAccessToken
import me.snitchon.parameters.header
import me.snitchon.parameters.path
import me.snitchon.parameters.pathParam

object Paths {
    val userId = path("userId", condition = NonEmptyString)
    val postId by pathParam(NonEmptyString)
}

object Headers {
    val accessToken = header(
        "X-Access-Token",
        condition = validAccessToken,
        description = "Access token for the principal user"
    )
}
