package snitch.types

import snitch.parameters.PathParam
import snitch.service.SnitchService

interface Routed {
    val config: snitch.config.SnitchConfig
    val service: SnitchService
    val pathParams: Set<PathParam<out Any, out Any>>
    val endpoints: MutableList<EndpointBundle<*>>
    val parser: Parser
    val path: String
}