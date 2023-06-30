package snitch.example.api

import me.snitch.parameters.header
import me.snitch.parameters.path
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
