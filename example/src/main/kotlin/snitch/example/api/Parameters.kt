package snitch.example.api

import snitch.parameters.header
import snitch.parameters.path
import snitch.example.api.validation.validAccessToken

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
