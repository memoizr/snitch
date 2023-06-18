package me.snitchon.example

import me.snitchon.parsers.GsonJsonParser
import me.snitchon.router.Router
import undertow.snitch.spark.undertow

fun Router.logged(routes: Router.() -> Unit) {
    applyToAll(routes) {
        doBefore {
            val method = method().name
            val path = undertow.exchange.requestPath
            ApplicationModule.logger().info("Begin Request: $method $path")
        }.doAfter {
            val method = method().name
            val path = undertow.exchange.requestPath
            ApplicationModule.logger().info("End Request: $method $path ${it.statusCode.code} ${it.value(GsonJsonParser)}")
        }
    }
}