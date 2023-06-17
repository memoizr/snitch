package me.snitchon.example

import me.snitchon.example.api.health.healthController
import me.snitchon.example.api.users.usersController
import me.snitchon.router.routes

val rootRouter = routes {
    "health" / healthController
    "users" / usersController
}
