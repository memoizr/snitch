package me.snitch.documentation

data class DocumentationConfig(
    val description: String = "",
    val title: String = "",
    val host: String = "http://localhost:3000",
    val port: Int = 3000,
    val basePath: String = "",
    val docPath: String = "swagger-spec",
    val termsOfService: String? = null,
    val contact: ConfigContact? = null,
    val license: ConfigLicense? = null,
    val project: Project? = null,
    val externalDoc: ExternalDoc? = null,
    val schemes: List<Scheme> = listOf(Scheme.HTTP),
    val theme: Theme = Theme.MATERIAL,
    val deepLinking: Boolean = false,
    val displayOperationId: Boolean = false,
    val defaultModelsExpandDepth: Int = 1,
    val defaultModelExpandDepth: Int = 1,
    val defaultModelRendering: ModelRendering = ModelRendering.model,
    val displayRequestDuration: Boolean = false,
    val docExpansion: DocExpansion = DocExpansion.FULL,
    val filter: Boolean = false,
    val showExtensions: Boolean = true,
    val showCommonExtensions: Boolean = true,
    val operationsSorter: String = "alpha",
    val tagsSorter: String = "alpha"
)

data class ConfigContact(val name: String, val email: String, val url: String)

data class ConfigLicense(val name: String, val url: String)

data class Project(val groupId: String, val artifactId: String)

data class ExternalDoc(val description: String, val url: String)

enum class DocExpansion { LIST, FULL, NONE }
enum class Theme { OUTLINE, FEELING_BLUE, FLATTOP, MATERIAL, MONOKAI, MUTED, NEWSPAPER }
enum class ModelRendering { example, model }

enum class Scheme(val value: String) {
    HTTP("http"),
    HTTPS("https"),
    WS("ws"),
    WSS("wss");
}
