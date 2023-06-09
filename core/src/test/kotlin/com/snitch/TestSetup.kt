package com.snitch

import me.snitchon.service.RoutedService
import com.snitch.spark.UndertowSnitchService
import me.snitchon.Config
import me.snitchon.Router
import me.snitchon.parameters.InvalidParametersException
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.response.badRequest
import me.snitchon.service.exceptionhandling.handleInvalidParameters
import me.snitchon.service.exceptionhandling.handleUnregisteredParameters
import me.snitchon.types.ErrorResponse

fun routes(router: Router.() -> Unit): (Int) -> RoutedService = { port ->
    UndertowSnitchService(
        Config(
            basePath = root,
            port = port
        ),
        GsonJsonParser
    ).setRoutes(router)
}