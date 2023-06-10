package me.snitchon.types

import me.snitchon.documentation.Config
import me.snitchon.parameters.PathParam
import me.snitchon.parsing.Parser
import me.snitchon.service.SnitchService

interface Routed {
    val config: Config
    val service: SnitchService
    val pathParams: Set<PathParam<out Any, out Any>>
    val endpoints: MutableList<EndpointBundle<*>>
    val parser: Parser
}