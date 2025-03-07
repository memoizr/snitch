package snitch.example

import snitch.etc.cors
import snitch.example.api.health.healthController
import snitch.example.api.users.usersController
import snitch.router.plus
import snitch.router.routes

val rootRouter = routes {
    (logged + cors) {
        "health" / healthController
        "users" / usersController
    }
}
