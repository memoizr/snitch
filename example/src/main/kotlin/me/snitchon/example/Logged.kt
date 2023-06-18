package me.snitchon.example

import me.snitchon.example.ApplicationModule.logger
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.router.Router
import me.snitchon.router.using
import undertow.snitch.spark.undertow

val Router.logged
    get() = using {
        val method = method.name
        logger().info("Begin Request: $method $path")
        next().also {
            logger().info("End Request: $method $path ${it.statusCode.code} ${it.value(parser)}")
        }
    }
