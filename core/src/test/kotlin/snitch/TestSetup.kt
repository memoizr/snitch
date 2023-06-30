package snitch

import snitch.undertow.UndertowSnitchService
import snitch.parsers.GsonJsonParser
import snitch.service.RoutedService
import snitch.config.SnitchConfig
import snitch.router.Routes

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