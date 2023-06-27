package me.snitchon

import undertow.snitch.spark.UndertowSnitchService
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.service.RoutedService
import me.snitchon.config.SnitchConfig
import me.snitchon.router.Routes

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