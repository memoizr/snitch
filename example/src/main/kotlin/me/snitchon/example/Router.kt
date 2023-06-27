package me.snitchon.example

import me.snitchon.example.ApplicationModule.logger
import me.snitchon.example.api.health.healthController
import me.snitchon.example.api.users.usersController
import me.snitchon.router.Router
import me.snitchon.router.routes
import me.snitchon.router.decorateWith

val Router.log get() = decorateWith {
        logger().info("Begin Request: ${request.method.name} ${request.path}")
        next().also {
            logger().info("End Request: ${request.method.name} ${request.path} ${it.statusCode.code} ${it.value(parser)}")
        }
    }

val rootRouter = routes {
    log {
        "health" / healthController
        "users" / usersController
    }
}
