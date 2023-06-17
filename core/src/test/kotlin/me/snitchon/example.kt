package me.snitchon

import undertow.snitch.spark.UndertowSnitchService
import me.snitchon.documentation.generateDocumentation
import me.snitchon.documentation.servePublicDocumenation
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser

fun main(args: Array<String>) {
    UndertowSnitchService(GsonJsonParser)
        .setRoutes(ServerRouter)
        .start()
        .generateDocumentation(GsonDocumentationSerializer)
        .servePublicDocumenation()
}

fun readmeExamples() {
    UndertowSnitchService(GsonJsonParser)
        .setRoutes {
            GET("/hello").isHandledBy { "world".ok }
        }
        .start()
        .generateDocumentation()
        .servePublicDocumenation()
}
