package me.snitchon.example.api.health

import me.snitchon.router.routes

val healthController = routes {
    GET("/liveness").isHandledBy { "ok".ok }
}