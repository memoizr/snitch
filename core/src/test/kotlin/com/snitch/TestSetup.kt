package com.snitch

import me.snitchon.service.RoutedService
import com.snitch.spark.UndertowSnitchService
import me.snitchon.documentation.Config
import me.snitchon.Router
import me.snitchon.documentation.generateDocs
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser

fun routes(router: Router.() -> Unit): (Int) -> RoutedService = { port ->
    UndertowSnitchService(
        Config(
            basePath = root,
            port = port
        ),
        GsonJsonParser
    ).setRoutes(router)
        .also {
            it.generateDocs(GsonDocumentationSerializer)
        }
}