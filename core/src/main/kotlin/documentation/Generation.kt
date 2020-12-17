package com.snitch.documentation

import RoutedService
import com.snitch.Parameter
import com.snitch.Router
import com.snitch.extensions.json
import java.io.File
import java.io.FileOutputStream
import kotlin.reflect.full.starProjectedType

fun RoutedService.generateDocs(): Spec {
    val openApi = OpenApi(info = Info(router.config.title, "1.0"), servers = listOf(Server(router.config.host)))
    return router.endpoints
            .groupBy { it.endpoint.url }
            .map { entry ->
                entry.key to entry.value.foldRight(Path()) { bundle: Router.EndpointBundle<*>, path ->
                    path.withOperation(
                            bundle.endpoint.httpMethod,
                            Operation(
                                    tags = bundle.endpoint.url.split("/").drop(1).firstOrNull()?.let { listOf(it) },
                                    summary = bundle.endpoint.summary,
                                    description = bundle.endpoint.description,
                                    responses = emptyMap(),
                                    visibility = bundle.endpoint.visibility
                            )
                                    .withResponse(ContentType.APPLICATION_JSON, bundle.response, "200")
                                    .let {
                                        if (bundle.endpoint.body.klass != Nothing::class) {
                                            it.withRequestBody(ContentType.APPLICATION_JSON, bundle.endpoint.body.klass)
                                        } else it
                                    }
                                    .let {
                                        bundle.endpoint.headerParams.fold(it) { acc, p ->
                                            acc.withParameter(
                                                Parameters.HeaderParameter(
                                                    name = p.name,
                                                    required = p.required,
                                                    description = getDescription(p),
                                                    visibility = p.visibility,
                                                    schema = toSchema(p.type.kotlin.starProjectedType)
                                                        .withPattern(p.pattern.regex)

                                                )
                                            )
                                        }
                                    }
                                    .let {
                                        bundle.endpoint.pathParams.fold(it) { acc, param ->
                                            acc.withParameter(
                                                Parameters.PathParameter(
                                                    name = param.name,
                                                    description = getDescription(param),
                                                    schema = toSchema(param.type.kotlin.starProjectedType)
                                                        .withPattern(param.pattern.regex)
                                                )
                                            )
                                        }
                                    }
                                    .let {
                                        bundle.endpoint.queryParams.fold(it) { acc, p ->
                                            acc.withParameter(
                                                Parameters.QueryParameter(
                                                    name = p.name,
                                                    description = getDescription(p),
                                                    allowEmptyValue = p.emptyAsMissing,
                                                    required = p.required,
                                                    visibility = p.visibility,
                                                    schema = toSchema(p.type.kotlin.starProjectedType)
                                                        .withPattern(p.pattern.regex)

                                                )
                                            )
                                        }
                                    }
                    )
                }
            }.fold(openApi) { a, b -> a.withPath(b.first, b.second) }
            .let { Spec(it.json, router) }
}

data class Spec(val spec: String, val router: Router) {

    fun writeDocsToStaticFolder() {
        val dest = "/tmp/swagger-ui" + "/docs"
        copyResourceToFile("favicon-16x16.png", dest)
        copyResourceToFile("favicon-32x32.png", dest)
        copyResourceToFile("index.html", dest)
        copyResourceToFile("oauth2-redirect.html", dest)
        copyResourceToFile("swagger-ui.css", dest)
        copyResourceToFile("swagger-ui.css.map", dest)
        copyResourceToFile("swagger-ui.js", dest)
        copyResourceToFile("swagger-ui.js.map", dest)
        copyResourceToFile("swagger-ui-bundle.js", dest)
        copyResourceToFile("swagger-ui-bundle.js.map", dest)
        copyResourceToFile("swagger-ui-es-bundle.js", dest)
        copyResourceToFile("swagger-ui-es-bundle.js.map", dest)
        copyResourceToFile("swagger-ui-es-bundle-core.js", dest)
        copyResourceToFile("swagger-ui-es-bundle-core.js.map", dest)
        copyResourceToFile("swagger-ui-standalone-preset.js", dest)
        copyResourceToFile("swagger-ui-standalone-preset.js.map", dest)
        writeToFile(spec, "$dest/spec.json")
    }
}

private fun getDescription(param: Parameter<*>) =
        "${param.description} - ${param.pattern.description}${if (param.invalidAsMissing) " - Invalid as Missing" else ""}${if (param.emptyAsMissing) " - Empty as Missing" else ""}"

internal fun writeToFile(content: String, destination: String) {
    File(destination.split("/").dropLast(1).joinToString("")).apply { if (!exists()) mkdirs() }
    content.byteInputStream().use { input ->
        FileOutputStream(destination).use { output ->
            input.copyTo(output)
        }
    }
}

internal fun copyResourceToFile(resourceName: String, destination: String) {
    val stream = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName)
            ?: throw Exception("Cannot get resource \"$resourceName\" from Jar file.")
    val s = "$destination/$resourceName"
    File(destination).apply { if (!exists()) mkdirs() }
    val resStreamOut = FileOutputStream(s)
    stream.use { input ->
        resStreamOut.use { output ->
            input.copyTo(output)
        }
    }
}
