package snitch.bootstrap

import snitch.config.SnitchConfig
import snitch.parsers.GsonJsonParser

fun snitch(port: Int) = snitch.undertow.snitch(GsonJsonParser, SnitchConfig(SnitchConfig.Service(port)))