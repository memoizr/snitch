package snitch.authorization

import snitch.parameters.header

object ValidationHeaders {
    val accessToken by header(
        condition = validAccessToken,
        "X-Access-Token",
        description = "Access token for the principal user"
    )
}