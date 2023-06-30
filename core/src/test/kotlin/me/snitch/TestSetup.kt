package me.snitch

import undertow.snitch.spark.UndertowSnitchService
import me.snitch.parsers.GsonJsonParser
import me.snitch.service.RoutedService
import me.snitch.config.SnitchConfig
import me.snitch.router.Routes

fun testRoutes(basePath: String = "", router: Routes): (Int) -> RoutedService = { port ->
    UndertowSnitchService(
        GsonJsonParser,
        SnitchConfig(
            SnitchConfig.Service(
                basePath = basePath,
                port = port
            )
        )
    ).onRoutes(router)
}