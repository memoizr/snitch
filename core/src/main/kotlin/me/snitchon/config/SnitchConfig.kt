package me.snitchon.config

import org.yaml.snakeyaml.Yaml
import java.io.File

data class SnitchConfig(var service: Service = Service()) {

    data class Service(
        var port: Int = 3000,
        var basePath: String = ""
    )
}

fun loadConfigFromFile(path: String): SnitchConfig {
    val yaml = Yaml()
    val text = File(path).readText()
    val config = yaml.loadAs(parse(text), SnitchConfig::class.java)
    return config
}

internal fun parse(string: String): String {
    val envs = System.getenv()
    val regex = "\\{[^}]*\\}".toRegex()
    val chunks = string.splitToSequence(regex)

    val substitutions = regex
        .findAll(string)
        .map { it.value.drop(1).dropLast(1) }
        .map {
            val (variable, default) = it.split(":") + null
            envs[variable] ?: default
        }

    val result = chunks
        .zip(substitutions + "")
        .filter { it.second != null }
        .flatMap { listOf(it.first, it.second) }
        .joinToString("")

    return result
}
