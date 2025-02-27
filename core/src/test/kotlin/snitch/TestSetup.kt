package snitch

import snitch.config.SnitchConfig
import snitch.parsers.GsonJsonParser
import snitch.router.Routes
import snitch.service.RoutedService
import snitch.undertow.UndertowSnitchService

fun testRoutes(
    basePath: String = "",
    transform: RoutedService.() -> RoutedService = { this },
    router: Routes,
): (Int) -> RoutedService = { port ->
    UndertowSnitchService(
        GsonJsonParser,
        SnitchConfig(
            SnitchConfig.Service(
                basePath = basePath,
                port = port
            )
        )
    ).onRoutes(router)
        .transform()
}