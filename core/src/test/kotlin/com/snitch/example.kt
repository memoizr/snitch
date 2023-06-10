package com.snitch

import com.snitch.spark.UndertowSnitchService
import me.snitchon.documentation.generateDocs
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser

fun main(args: Array<String>) {
    UndertowSnitchService(GsonJsonParser)
        .setRoutes(ServerRouter)
        .startListening()
        .generateDocs(GsonDocumentationSerializer)
        .servePublicDocumenation()
}
