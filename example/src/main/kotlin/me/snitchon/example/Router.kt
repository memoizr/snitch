package me.snitchon.example

import me.snitchon.Router
import me.snitchon.example.api.users.usersController

val router: Router.() -> Unit = {
    GET("/health/liveness")
        .isHandledBy { "ok".ok }
    usersController()
}