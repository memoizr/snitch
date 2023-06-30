package snitch.example.api.health

import snitch.router.routes

val healthController = routes {
    GET("/liveness").isHandledBy { "ok".ok }
}