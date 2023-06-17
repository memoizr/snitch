package me.snitchon.example

import me.snitchon.Router
import me.snitchon.example.api.auth.authenticated
import me.snitchon.example.api.users.usersController

val router: Router.() -> Unit = {
    apply({ authenticated() }) {
        GET("/health/liveness")
            .isHandledBy { "ok".ok }
        usersController()
    }
}
