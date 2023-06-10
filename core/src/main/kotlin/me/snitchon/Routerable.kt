package me.snitchon

import me.snitchon.parameters.PathParam
import me.snitchon.parsing.Parser
import me.snitchon.service.SnitchService

interface Routerable {
    val config: Config
    val service: SnitchService
    val pathParams: Set<PathParam<out Any, out Any>>
    val endpoints: MutableList<EndpointBundle<*>>
    val parser: Parser
}