package com.snitch

import com.snitch.spark.UndertowSnitchService
import io.undertow.Undertow
import me.snitchon.documentation.generateDocs
import me.snitchon.extensions.print
//import com.snitch.spark.SparkSnitchService
import me.snitchon.Config
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser

fun main(args: Array<String>) {
    UndertowSnitchService(Config(), GsonJsonParser)
        .setRoutes(ServerRouter)
        .startListening()
        .generateDocs(GsonDocumentationSerializer)
        .print()
        .servePublicDocumenation()
}
