package com.snitch

import me.snitchon.documentation.generateDocs
import me.snitchon.extensions.print
import com.snitch.spark.SparkSnitchService
import me.snitchon.Config
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser

fun main(args: Array<String>) {
    SparkSnitchService(Config(), GsonJsonParser)
        .setRoutes(ServerRouter).generateDocs(GsonDocumentationSerializer)
        .print()
        .writeDocsToStaticFolder()
}
