package com.snitch

import me.snitchon.RoutedService
import com.snitch.spark.UndertowSnitchService
import me.snitchon.Config
import me.snitchon.Router
import me.snitchon.parsers.GsonJsonParser

fun routes(router: Router.() -> Unit): (Int) -> RoutedService =
    { port ->
//        SparkSnitchService(
//            Config(
//                basePath = root,
//                port = port
//            ),
//            GsonJsonParser
//        ).setRoutes(router)
        UndertowSnitchService(
            Config(
                basePath = root,
                port = port
            ),
            GsonJsonParser
        ).setRoutes(router)
    }