package com.snitch

import com.snitch.documentation.generateDocs
import com.snitch.extensions.print
import com.snitch.spark.SparkSnitchService
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser

fun main(args: Array<String>) {
    SparkSnitchService(Config(), GsonJsonParser)
        .setRoutes(ServerRouter).generateDocs(GsonDocumentationSerializer)
        .print()
        .writeDocsToStaticFolder()
}
