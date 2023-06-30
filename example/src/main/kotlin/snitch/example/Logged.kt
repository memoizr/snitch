package snitch.example

import snitch.router.Router
import snitch.router.decorateWith
import snitch.example.ApplicationModule.logger

val Router.logged
    get() = decorateWith {
        val method = method.name
        logger().info("Begin Request: $method $path")
        next().also {
            logger().info("End Request: $method $path ${it.statusCode.code} ${it.value(parser)}")
        }
    }
