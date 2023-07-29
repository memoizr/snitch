package snitch.config

import org.yaml.snakeyaml.Yaml
import java.io.File

data class SnitchConfig(var service: snitch.config.SnitchConfig.Service = snitch.config.SnitchConfig.Service()) {

    data class Service(
        var port: Int = 3000,
        var host: String = "0.0.0.0",
        var basePath: String = ""
    )
}

fun loadConfigFromFile(path: String): snitch.config.SnitchConfig {
    val yaml = Yaml()
    val text = File(path).readText()
    val config = yaml.loadAs(snitch.config.parse(text), snitch.config.SnitchConfig::class.java)
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
