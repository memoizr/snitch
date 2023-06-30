package me.snitch.types

import me.snitch.parameters.PathParam
import me.snitch.parsing.Parser
import me.snitch.config.SnitchConfig
import me.snitch.service.SnitchService

interface Routed {
    val config: SnitchConfig
    val service: SnitchService
    val pathParams: Set<PathParam<out Any, out Any>>
    val endpoints: MutableList<EndpointBundle<*>>
    val parser: Parser
    val path: String
}