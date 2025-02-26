package snitch.example

import snitch.example.api.health.healthController
import snitch.example.api.users.usersController
import snitch.router.routes

val rootRouter = routes {
    logged {
        "health" / healthController
        "users" / usersController
    }
}
