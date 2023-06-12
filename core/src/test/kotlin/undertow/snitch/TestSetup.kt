package undertow.snitch

import undertow.snitch.spark.UndertowSnitchService
import me.snitchon.Router
import me.snitchon.documentation.generateDocumentation
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.service.RoutedService
import me.snitchon.config.SnitchConfig

fun routes(router: Router.() -> Unit): (Int) -> RoutedService = { port ->
    UndertowSnitchService(
        GsonJsonParser,
        SnitchConfig(
            SnitchConfig.Service(
                basePath = root,
                port = port
            )
        )
    ).setRoutes(router)
        .also {
            it.generateDocumentation(GsonDocumentationSerializer)
        }
}