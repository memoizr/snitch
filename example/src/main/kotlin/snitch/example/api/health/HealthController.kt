package snitch.example.api.health

import me.snitch.router.routes

val healthController = routes {
    GET("/liveness").isHandledBy { "ok".ok }
}