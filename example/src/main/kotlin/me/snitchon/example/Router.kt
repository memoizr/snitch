package me.snitchon.example

import me.snitchon.example.ApplicationModule.logger
import me.snitchon.example.api.health.healthController
import me.snitchon.example.api.users.usersController
import me.snitchon.router.Router
import me.snitchon.router.routes
import me.snitchon.router.using
import undertow.snitch.spark.undertow

val Router.log
    get() = using {
        val method = method().name
        val path = wrap.undertow.exchange.requestPath
        logger().info("Begin Request: $method $path")
        next().also {
            logger().info("End Request: $method $path ${it.statusCode.code} ${it.value(parser)}")
        }
    }

val rootRouter = routes {
    log {
        "health" / healthController
        "users" / usersController
    }
}
