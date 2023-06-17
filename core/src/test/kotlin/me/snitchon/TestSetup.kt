package me.snitchon

import undertow.snitch.spark.UndertowSnitchService
import me.snitchon.documentation.generateDocumentation
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.service.RoutedService
import me.snitchon.config.SnitchConfig
import me.snitchon.router.Router

fun testRoutes(basePath: String = "", router: Router.() -> Unit): (Int) -> RoutedService = { port ->
    UndertowSnitchService(
        GsonJsonParser,
        SnitchConfig(
            SnitchConfig.Service(
                basePath = basePath,
                port = port
            )
        )
    ).setRoutes(router)
}