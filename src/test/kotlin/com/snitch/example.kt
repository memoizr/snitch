package com.snitch

import com.snitch.documentation.generateDocs
import com.snitch.spark.SparkSnitchService

fun main(args: Array<String>) {
    SparkSnitchService(Config()).setRoutes(ServerRouter).generateDocs().writeDocsToStaticFolder()
}
