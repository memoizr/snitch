package com.snitch.documentation

import RoutedService
import com.snitch.Parameter
import com.snitch.Router
import documentation.DefaultDocumentatoinSerializer
import documentation.DocumentationSerializer
import java.io.File
import java.io.FileOutputStream
import kotlin.reflect.full.starProjectedType

fun RoutedService.generateDocs(documentationSerializer: DocumentationSerializer = DefaultDocumentatoinSerializer): Spec {
    val openApi = OpenApi(info = Info(router.config.title, "1.0"), servers = listOf(Server(router.config.host)))
    return router.endpoints
            .groupBy { it.endpoint.url }
            .map { entry ->
                entry.key to entry.value.foldRight(Path()) { bundle: Router.EndpointBundle<*>, path ->
                    path.withOperation(
                            bundle.endpoint.httpMethod,
                            Operation(
                                    tags = bundle.endpoint.tags,
                                    summary = bundle.endpoint.summary,
                                    description = bundle.endpoint.description,
                                    responses = emptyMap(),
                                    visibility = bundle.endpoint.visibility
                            )
                                    .withResponse(documentationSerializer, ContentType.APPLICATION_JSON, bundle.response, "200")
                                    .let {
                                        if (bundle.endpoint.body.klass != Nothing::class) {
                                            it.withRequestBody(documentationSerializer, ContentType.APPLICATION_JSON, bundle.endpoint.body.klass)
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
                                                    schema = toSchema(documentationSerializer, p.type.kotlin.starProjectedType)
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
                                                    schema = toSchema(documentationSerializer, param.type.kotlin.starProjectedType)
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
                                                    schema = toSchema(documentationSerializer, p.type.kotlin.starProjectedType)
                                                        .withPattern(p.pattern.regex)

                                                )
                                            )
                                        }
                                    }
                    )
                }
            }.fold(openApi) { a, b -> a.withPath(b.first, b.second) }
            .let { Spec(with( router.parser) {it.jsonString }, router) }
}

data class Spec(val spec: String, val router: Router) {

    fun writeDocsToStaticFolder() {
        val dest = "/tmp/swagger-ui" + "/docs"
        writeToFile(spec, "$dest/spec.json")
        File("$dest/index.html").writeText(index)
    }
}

private fun getDescription(param: Parameter<*,*>) =
        "${param.description} - ${param.pattern.description}${if (param.invalidAsMissing) " - Invalid as Missing" else ""}${if (param.emptyAsMissing) " - Empty as Missing" else ""}"

internal fun writeToFile(content: String, destination: String) {
    File(destination.split("/").dropLast(1).joinToString("")).apply { if (!exists()) mkdirs() }
    content.byteInputStream().use { input ->
        FileOutputStream(destination).use { output ->
            input.copyTo(output)
        }
    }
}

val index = """
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <title>Swagger UI</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@3.44.0/swagger-ui.css">
    <link rel="icon" type="image/png" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@3.44.0/favicon-32x32.png" sizes="32x32" />
    <link rel="icon" type="image/png" href="https://cdn.jsdelivr.net/npm/swagger-ui-dist@3.44.0/favicon-32x32.png" sizes="16x16" />
    <style>
      html {
        box-sizing: border-box;
        overflow: -moz-scrollbars-vertical;
        overflow-y: scroll;
      }

      *, *:before, *:after {
        box-sizing: inherit;
      }

      body {
        margin:0;
        background: #fafafa;
      }
    </style>
  </head>

  <body>
    <div id="swagger-ui"></div>

    <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@3.44.0/swagger-ui-standalone-preset.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/swagger-ui-dist@3.44.0/swagger-ui-bundle.js"></script>
    <script>
    window.onload = function() {
      // Begin Swagger UI call region
      const ui = SwaggerUIBundle({
        url: "./spec.json",
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [
          SwaggerUIBundle.presets.apis,
          SwaggerUIStandalonePreset
        ],
        plugins: [
          SwaggerUIBundle.plugins.DownloadUrl
        ],
        layout: "StandaloneLayout"
      });
      // End Swagger UI call region

      window.ui = ui;
    };
  </script>
  </body>
</html>
"""