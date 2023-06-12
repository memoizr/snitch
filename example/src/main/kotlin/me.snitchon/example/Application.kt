package me.snitchon.example

import com.snitch.spark.UndertowSnitchService
import me.snitchon.Router
import me.snitchon.config.SnitchConfig
import me.snitchon.config.SnitchConfig.Service
import me.snitchon.documentation.generateDocumentation
import me.snitchon.documentation.servePublicDocumenation
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.service.RoutedService

object Application {
    private lateinit var server: RoutedService

    fun start(port: Int) = UndertowSnitchService(
        GsonJsonParser,
        SnitchConfig(Service(port = port))
    )
        .setRoutes(router)
        .also { server = it }

    fun stop() {
        server.stop()
    }
}

val router: Router.() -> Unit = {
    GET("/health/liveness")
        .isHandledBy { "ok".ok }
}