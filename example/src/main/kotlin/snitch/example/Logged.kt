package snitch.example

import me.snitch.router.Router
import me.snitch.router.decorateWith
import snitch.example.ApplicationModule.logger

val Router.logged
    get() = decorateWith {
        val method = method.name
        logger().info("Begin Request: $method $path")
        next().also {
            logger().info("End Request: $method $path ${it.statusCode.code} ${it.value(parser)}")
        }
    }
