package me.snitchon.example

import me.snitchon.example.ApplicationModule.logger
import me.snitchon.router.Router
import me.snitchon.router.decorateWith

val Router.logged
    get() = decorateWith {
        val method = method.name
        logger().info("Begin Request: $method $path")
        next().also {
            logger().info("End Request: $method $path ${it.statusCode.code} ${it.value(parser)}")
        }
    }
