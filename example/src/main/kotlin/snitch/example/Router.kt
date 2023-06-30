package snitch.example

import snitch.router.Router
import snitch.router.decorateWith
import snitch.router.routes
import snitch.example.ApplicationModule.logger
import snitch.example.api.health.healthController
import snitch.example.api.users.usersController

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
