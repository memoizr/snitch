package snitch.exposed

import snitch.config.SnitchConfig
import snitch.parsers.GsonJsonParser
import snitch.router.Routes
import snitch.service.RoutedService
import snitch.undertow.UndertowSnitchService

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