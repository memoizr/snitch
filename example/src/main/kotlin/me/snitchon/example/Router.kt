package me.snitchon.example

import me.snitchon.Router
import me.snitchon.example.api.auth.authenticated
import me.snitchon.example.api.users.usersController
import me.snitchon.routes

val router: Router.() -> Unit = {
    "health" / healthController
    "users" / usersController
}

val healthController = routes {
    GET("/liveness").isHandledBy { "ok".ok }
}
