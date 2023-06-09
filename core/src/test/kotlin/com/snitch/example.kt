package com.snitch

import com.snitch.spark.UndertowSnitchService
import me.snitchon.Config
import me.snitchon.documentation.generateDocs
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.response.ok

fun main(args: Array<String>) {
    UndertowSnitchService(Config(), GsonJsonParser)
        .setRoutes({
                GET("foo").isHandledBy { "no".ok }
        })
        .startListening()
        .generateDocs(GsonDocumentationSerializer)
        .servePublicDocumenation()
}
