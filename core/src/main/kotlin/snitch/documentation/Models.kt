package snitch.documentation

import java.util.*

internal data class OpenApi(
    val openapi: String = "3.0.0",
    val info: Info,
    val paths: Map<String, Path> = emptyMap(),
    val servers: List<Server>? = null,
    val components: Components? = null,
    val security: Map<String, List<String>>? = null,
    val tags: List<Tag>? = null,
    val externalDocs: ExternalDocumentation? = null
)

internal data class Components(
    val schemas: Map<String, Schemas>,
    val responses: Map<String, Responses>,
    val parameters: Map<String, Parameters>,
    val examples: Map<String, Examples>,
    val requestBodies: Map<String, RequestBodies>,
    val headers: Map<String, Headers>,
    val securitySchemes: Map<String, SecuritySchemes>,
    val links: Map<String, Links>
)

internal interface Ref {
    val `$ref`: String
}

internal sealed class Schemas {
    fun withPattern(pattern: Regex?): Schemas {
        return when (this) {
            is BaseSchema<*> -> this.apply { this.pattern = pattern?.pattern }
            else -> this
        }
    }

    fun withVisibility(visibility: Visibility?): Schemas {
        return when (this) {
            is BaseSchema<*> -> this.apply { this.visibility = visibility }
            else -> this
        }
    }

    internal abstract class BaseSchema<T : Any>(
        open val type: DataType,
        open val format: Format? = null,
        var pattern: String? = null,
        var nullable: Boolean? = null,
        var visibility: Visibility? = Visibility.PUBLIC
    ) : Schemas() {
        abstract var description: String?
    }

    internal data class ArraySchema(
        val items: Schemas,
        val maxItems: Int? = null,
        val minItems: Int? = null,
        val uniqueItems: Boolean? = null,
        var example: Any? = null,
        val default: List<*>? = null,
        override var description: String? = null
    ) : BaseSchema<List<*>>(type = DataType.array)

    internal data class StringSchema(
        override var description: String? = null,
        val default: String? = null,
        var example: Any? = null,
        val enum: List<String>? = null
    ) : BaseSchema<String>(type = DataType.string)

    internal data class IntSchema(
        override var description: String? = null,
        val maximum: Int? = null,
        val minimum: Int? = null,
        var example: Any? = null,
        val default: Int? = null
    ) : BaseSchema<Int>(type = DataType.integer, format = Format.int32)

    internal data class LongSchema(
        override var description: String? = null,
        val default: Long? = null,
        val maximum: Long? = null,
        var example: Any? = null,
        val minimum: Long? = null
    ) : BaseSchema<Long>(type = DataType.integer, format = Format.int64)

    internal data class DoubleSchema(
        override var description: String? = null,
        val default: Double? = null,
        val maximum: Double? = null,
        var example: Any? = null,
        val minimum: Double? = null
    ) : BaseSchema<Double>(type = DataType.number, format = Format.double)

    internal data class FloatSchema(
        override var description: String? = null,
        val default: Float? = null,
        val maximum: Double? = null,
        var example: Any? = null,
        val minimum: Double? = null
    ) : BaseSchema<Float>(type = DataType.number, format = Format.float)

    internal data class ByteSchema(
        override var description: String? = null,
        val default: Byte? = null
    ) : BaseSchema<Byte>(type = DataType.string, format = Format.byte)

    internal data class BinarySchema(
        override var description: String? = null,
        val default: ByteArray? = null
    ) : BaseSchema<ByteArray>(type = DataType.string, format = Format.byte)

    internal data class BooleanSchema(
        override var description: String? = null,
        val default: Boolean? = null
    ) : BaseSchema<Boolean>(type = DataType.boolean)

    internal data class DateSchema(
        override var description: String? = null,
        val default: Date? = null
    ) : BaseSchema<Date>(type = DataType.string, format = Format.date)

    internal data class DateTimeSchema(
        override var description: String? = null,
        val default: Date? = null
    ) : BaseSchema<Date>(type = DataType.string, format = Format.`date-time`)

    internal data class PasswordSchema(
        override var description: String? = null,
        val default: String? = null
    ) : BaseSchema<String>(type = DataType.string, format = Format.password)

    internal data class ObjectSchema(
        val allOf: List<Schemas>? = null,
        val oneOf: List<Schemas>? = null,
        val anyOf: List<Schemas>? = null,
        val not: List<Schemas>? = null,
        val items: Schemas? = null,
        val properties: Map<String, Schemas>? = null,
        val additionalProperties: Schemas? = null,
        override var description: String? = null,
        val default: Any? = null,
        var example: Any? = null,
        val required: List<String>? = null
    ) : BaseSchema<Any>(type = DataType.`object`)

    internal data class Reference(override val `$ref`: String) : Schemas(), Ref
}

internal enum class Format { int32, int64, float, double, byte, binary, date, `date-time`, password }
internal enum class DataType { integer, number, string, boolean, array, `object` }

internal sealed class Responses {
    internal data class Reference(override val `$ref`: String) : Responses(), Ref
    internal data class Response(
        val description: String = "A response",
        val headers: Map<String, Headers>? = null,
        val content: Map<String, MediaType>? = null,
        val links: Map<String, Links>? = null
    ) : Responses()
}

internal data class MediaType(
    val schema: Schemas? = null,
    val example: Any? = null,
    val examples: Map<String, Examples>? = null,
    val encoding: Map<String, Encoding>? = null
)

internal data class Encoding(
    val contentType: String? = null,
    val headers: Map<String, Headers>? = null,
    val style: String? = null,
    val explode: Boolean? = null,
    val allowReserved: Boolean? = null
)

internal sealed class Headers {
    internal data class Reference(override val `$ref`: String) : Headers(), Ref
    internal data class Header(
        val name: String,
        val description: String? = null,
        val externalDocs: ExternalDocumentation? = null
    ) : Headers()
}

internal sealed class Links {
    internal data class Reference(override val `$ref`: String) : Links(), Ref
    internal data class Link(
        val operationRef: String? = null,
        val operationId: String? = null,
        val parameters: Map<String, Any>? = null,
        val requestBody: Any? = null,
        val description: String? = null,
        val server: Server?
    ) : Links()
}

internal enum class ParameterType { query, header, path, cookie }
internal sealed class Parameters {
    internal data class Reference(override val `$ref`: String) : Parameters(), Ref

    abstract class Parameter() : Parameters()

    internal data class QueryParameter(
        val name: String,
        val required: Boolean,
        val schema: Schemas,
        val description: String? = null,
        val deprecated: Boolean? = false,
        val allowEmptyValue: Boolean? = false,
        val visibility: Visibility? = Visibility.PUBLIC
    ) : Parameter() {
        val `in`: ParameterType = ParameterType.query
    }

    internal data class HeaderParameter(
        val name: String,
        val required: Boolean,
        val schema: Schemas,
        val description: String? = null,
        val deprecated: Boolean? = false,
        val visibility: Visibility? = Visibility.PUBLIC
    ) : Parameter() {
        val `in`: ParameterType = ParameterType.header
    }

    internal data class PathParameter(
        val name: String,
        val schema: Schemas,
        val description: String? = null,
        val deprecated: Boolean? = false
    ) : Parameter() {
        val `in`: ParameterType = ParameterType.path
        val required = true
    }

    internal data class CookieParameter(
        val name: String,
        val required: Boolean,
        val schema: Schemas,
        val description: String? = null,
        val deprecated: Boolean? = false
    ) : Parameter() {
        val `in`: ParameterType = ParameterType.cookie
    }
}

internal sealed class Examples {
    internal data class Reference(override val `$ref`: String) : Examples(), Ref
    internal data class Example(
        val summary: String? = null,
        val description: String? = null,
        val value: Any? = null,
        val externalValue: String? = null
    )
}

internal sealed class RequestBodies {
    internal data class Reference(override val `$ref`: String) : RequestBodies(), Ref
    internal data class RequestBody(
        val description: String? = null,
        val content: Map<String, MediaType>,
        val required: Boolean? = null
    ) : RequestBodies()
}

internal data class Path(
    val `$ref`: String? = null,
    val summary: String? = null,
    val description: String? = null,
    val get: Operation? = null,
    val put: Operation? = null,
    val post: Operation? = null,
    val delete: Operation? = null,
    val options: Operation? = null,
    val head: Operation? = null,
    val patch: Operation? = null,
    val trace: Operation? = null,
    val servers: List<Server>? = null,
    val parameters: Parameters? = null
)


internal data class Operation(
    val responses: Map<String, Responses>,
    val tags: List<String>? = null,
    val summary: String? = null,
    val description: String? = null,
    val externalDocs: ExternalDocumentation? = null,
    val operationId: String? = null,
    val parameters: List<Parameters>? = null,
    val requestBody: RequestBodies? = null,
    val deprecated: Boolean? = null,
    val security: Map<String, List<String>>? = null,
    val servers: List<Server>? = null,
    val visibility: Visibility?
)

internal data class Contact(val name: String? = null, val url: String? = null, val email: String? = null)
internal data class License(val name: String, val url: String? = null)
internal data class Server(
    val url: String,
    val description: String? = null,
    val variables: Map<String, ServerVariable>? = null
)

internal data class ServerVariable(val default: String, val enum: List<String>? = null, val description: String? = null)
internal data class Tag(
    val name: String,
    val description: String? = null,
    val externalDocs: ExternalDocumentation? = null
)

internal data class ExternalDocumentation(val url: String, val description: String? = null)

internal data class OAuthFlows(
    val implicit: OAuthFlow? = null,
    val password: OAuthFlow? = null,
    val clientCredentials: OAuthFlow? = null,
    val authorizationCode: OAuthFlow? = null
)

internal data class OAuthFlow(
    val authorizationUrl: String,
    val tokenUrl: String,
    val refreshUrl: String? = null,
    val scopes: Map<String, String>
)

internal sealed class SecuritySchemes {
    internal enum class In { query, `header`, cookie }
    internal enum class Type { apiKey, http, oauth2, openIdConnect }

    internal data class Reference(override val `$ref`: String) : SecuritySchemes(), Ref
    internal data class SecurityScheme(
        val type: Type,
        val description: String? = null,
        val name: String,
        val `in`: In,
        val scheme: String,
        val bearerFormat: String?,
        val flows: OAuthFlows,
        val openIdConnectUrl: String
    )
}

internal data class Info(
    val title: String,
    val version: String,
    val description: String? = null,
    val termsOfService: String? = null,
    val contact: Contact? = null,
    val license: License? = null
)

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Repeatable()
annotation class Description(
    val description: String = "",
    val exString: String = "",
    val exInt: Int = 0,
    val exLong: Long = 0,
    val exFloat: Float = 0.0f,
    val exDouble: Double = 0.0,
    val exEmptyList: Boolean = false,
    val pattern: String = "",
    val visibility: Visibility = Visibility.PUBLIC
)


enum class Visibility {
    PUBLIC, INTERNAL
}
