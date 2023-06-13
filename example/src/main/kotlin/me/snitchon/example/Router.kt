package me.snitchon.example

import me.snitchon.Router

val router: Router.() -> Unit = {
    GET("/health/liveness")
        .isHandledBy { "ok".ok }

    usersController()
}