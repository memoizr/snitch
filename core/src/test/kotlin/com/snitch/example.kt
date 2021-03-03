package com.snitch

import com.snitch.documentation.generateDocs
import com.snitch.extensions.print
import com.snitch.spark.SparkSnitchService

fun main(args: Array<String>) {
    SparkSnitchService(Config()).setRoutes(ServerRouter).generateDocs()
        .print()
        .writeDocsToStaticFolder()
}
