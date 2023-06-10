package com.snitch

import com.snitch.spark.UndertowSnitchService
import me.snitchon.documentation.generateDocumentation
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.response.ok

fun main(args: Array<String>) {
    UndertowSnitchService(GsonJsonParser)
        .setRoutes(ServerRouter)
        .startListening()
        .generateDocumentation(GsonDocumentationSerializer)
        .servePublicDocumenation()
}

fun readmeExamples() {
    UndertowSnitchService(GsonJsonParser)
        .setRoutes {
            GET("/hello").isHandledBy { "world".ok }
        }
        .startListening()
        .generateDocumentation()
        .servePublicDocumenation()
}
