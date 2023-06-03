package com.snitch

import RoutedService
import com.snitch.spark.SparkSnitchService

fun routes(router: Router.() -> Unit): (Int) -> RoutedService =
    { port ->
        SparkSnitchService(
            Config(
                basePath = root,
                port = port
            )
        ).setRoutes(router)
    }