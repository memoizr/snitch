interface PatchMethodSyntax: MethodSyntax {
    fun PATCH() = method(PATCH)
    infix fun PATCH(path: String) = method(PATCH, path)
    infix fun PATCH(path: ParametrizedPath) = method(PATCH, path)
    infix fun PATCH(path: PathParam<out Any, out Any>) = PATCH("" / path)
}

interface DivSyntax: Routed {
    operator fun String.div(path: String) = this.leadingSlash + "/" + path
    operator fun String.div(path: PathParam<out Any, out Any>) = ParametrizedPath(this + "/{${path.name}}", setOf(path))

    operator fun PathParam<out Any, out Any>.div(string: String) = ParametrizedPath("/{${name}}" + string.leadingSlash, pathParams + this)

    operator fun String.div(block: Routes) {
        val router = Router(config, service, pathParams, parser, path + this.leadingSlash)
        router.block()
        endpoints += router.endpoints.map {
            EndpointBundle(
                it.endpoint,
                it.response,
                it.handlerResponse,
                it.handler,
            )
        }
    }

    operator fun ParametrizedPath.div(block: Routes) {
        val router = Router(config, service, pathParams + this.pathParameters, parser, this@DivSyntax.path + this.path.leadingSlash)
        router.block()
        router.endpoints += router.endpoints.map {
            EndpointBundle(
                it.endpoint,
                it.response,
                it.handlerResponse,
                it.handler
            )
        }
        endpoints += router.endpoints
    }

    operator fun PathParam<out Any, out Any>.div(block: Routes) {
        val path = ParametrizedPath("/{$name}", setOf(this))
        val router = Router(config, service, pathParams + this, parser, this@DivSyntax.path + path.path.leadingSlash)
        router.block()
        endpoints += router.endpoints.map {
            EndpointBundle(
                it.endpoint.copy(pathParams = it.endpoint.pathParams),
                it.response,
                it.handlerResponse,
                it.handler
            )
        }
    }
}

interface MethodSyntax : Routed, DivSyntax

internal fun MethodSyntax.method(httpMethods: HTTPMethods) = Endpoint(
    httpMethod = httpMethods,
    summary = null,
    description = null,
    url = this.path,
    pathParams = pathParams,
    queryParams = emptySet(),
    headerParams = emptySet(),
    body = Body(Nothing::class)
)

internal fun MethodSyntax.method(httpMethods: HTTPMethods, path: String) = Endpoint(
    httpMethod = httpMethods,
    summary = null,
    description = null,
    url = this.path + path.leadingSlash,
    pathParams = pathParams,
    queryParams = emptySet(),
    headerParams = emptySet(),
    body = Body(Nothing::class)
)

internal fun MethodSyntax.method(httpMethods: HTTPMethods, path: ParametrizedPath) = Endpoint(
    httpMethod = httpMethods,
    summary = null,
    description = null,
    url = this.path + path.path.leadingSlash,
    pathParams = pathParams + path.pathParameters,
    queryParams = emptySet(),
    headerParams = emptySet(),
    body = Body(Nothing::class)
)

interface HttpMethodsSyntax :
    GetMethodSyntax,
    PostMethodSyntax,
    PutMethodSyntax,
    DeleteMethodSyntax,
    PatchMethodSyntax,
    OptionsMethodSyntax,
    HeadMethodSyntax

interface GetMethodSyntax : MethodSyntax {
    fun GET() = method(GET)
    infix fun GET(path: String) = method(GET, path)
    infix fun GET(path: ParametrizedPath) = method(GET, path)
    infix fun GET(path: PathParam<out Any, out Any>) = GET("" / path)
}

interface DeleteMethodSyntax : MethodSyntax {
    fun DELETE() = method(DELETE)
    infix fun DELETE(path: String) = method(DELETE, path)
    infix fun DELETE(path: ParametrizedPath) = method(DELETE, path)
    infix fun DELETE(path: PathParam<out Any, out Any>) = DELETE("" / path)
}

interface OptionsMethodSyntax : MethodSyntax {
    fun OPTIONS() = method(OPTIONS)
    infix fun OPTIONS(path: String) = method(OPTIONS, path)
    infix fun OPTIONS(path: ParametrizedPath) = method(OPTIONS, path)
    infix fun OPTIONS(path: PathParam<out Any, out Any>) = OPTIONS("" / path)
}

interface HeadMethodSyntax: MethodSyntax {
    fun HEAD() = method(HEAD)
    infix fun HEAD(path: String) = method(HEAD, path)
    infix fun HEAD(path: ParametrizedPath) = method(HEAD, path)
    infix fun HEAD(path: PathParam<out Any, out Any>) = HEAD("" / path)
}

interface PostMethodSyntax : MethodSyntax {
    fun POST() = method(POST)
    infix fun POST(path: String) = method(POST, path)
    infix fun POST(path: ParametrizedPath) = method(POST, path)
    infix fun POST(path: PathParam<out Any, out Any>) = POST("" / path)
}

interface PutMethodSyntax: MethodSyntax {
    fun PUT() = method(PUT)
    infix fun PUT(path: String) = method(PUT, path)
    infix fun PUT(path: ParametrizedPath) = method(PUT, path)
    infix fun PUT(path: PathParam<out Any, out Any>) = PUT("" / path)
}

typealias Routes = Router.() -> Unit

typealias Decoration = DecoratedWrapper.() -> HttpResponse<*, *>

typealias BeforeAction =  RequestWrapper.() -> Unit

typealias AfterAction = RequestWrapper.(HttpResponse<out Any, *>) -> Unit

typealias EndpointMap = Endpoint<*>.() -> Endpoint<*>

class Router(
    override val config: SnitchConfig,
    override val service: SnitchService,
    override val pathParams: Set<PathParam<out Any, out Any>> = emptySet(),
    override val parser: Parser,
    override val path: String,
) : HttpMethodsSyntax {

    override val endpoints = mutableListOf<EndpointBundle<*>>()

    inline fun <B : Any, reified T : Any, S : StatusCodes> Endpoint<B>.addEndpoint(
        endpointResponse: HandlerResponse<B, T, S>
    ): Endpoint<B> = apply {
        endpoints += EndpointBundle(
            this,
            EndpointResponse(endpointResponse.statusCodes, endpointResponse.type),
            endpointResponse as HandlerResponse<Any, Any, out StatusCodes>,
        ) { request ->
            decorator(
                DecoratedWrapper({
                    endpointResponse.handler(
                        parser,
                        TypedRequestWrapper(request)
                    )
                }, request)
            ).next()
        }
    }

    inline infix fun <B : Any, reified T : Any, S : StatusCodes> Endpoint<B>.isHandledBy(
        handlerResponse: HandlerResponse<B, T, S>
    ): Endpoint<B> = addEndpoint(handlerResponse)

    inline infix fun <B : Any, reified T : Any, reified S : StatusCodes> Endpoint<B>.isHandledBy(
        noinline handler: context(Parser) TypedRequestWrapper<B>.() -> HttpResponse<T, S>
    ): Endpoint<B> = addEndpoint(
        HandlerResponse(S::class.starProjectedType, T::class.starProjectedType, handler)
    )

    fun queries(vararg queryParameter: QueryParameter<*, *>) = queryParameter.asList()
    fun headers(vararg headerParameter: HeaderParameter<*, *>) = headerParameter.asList()
    fun description(description: String) = OpDescription(description)
    inline fun <reified T : Any> body(contentType: ContentType = ContentType.APPLICATION_JSON) =
        Body(T::class, contentType)

    fun applyToAll(routerConfig: Routes, action: Endpoint<*>.() -> Endpoint<*>) {
        val router = Router(config, service, pathParams, parser, path)
        router.routerConfig()

        endpoints += router.endpoints.map {
            val endpoint = it.endpoint.action()
            EndpointBundle(
                endpoint,
                EndpointResponse(it.handlerResponse.statusCodes, it.handlerResponse.type),
                it.handlerResponse,
            ) { request ->
                endpoint.decorator(
                    DecoratedWrapper({
                        it.handlerResponse.handler(
                            parser,
                            TypedRequestWrapper(request)
                        )
                    }, request)
                ).next()
            }
        }
    }
}

fun routes(routes: Routes) = routes

internal val String.leadingSlash get() = if (!startsWith("/")) "/$this" else this

fun Router.using(decoration: DecoratedWrapper.() -> HttpResponse<out Any, *>) = decorateAll { decorate(decoration) }

fun Router.decorateAll(action: Endpoint<*>.() -> Endpoint<*>): (Routes) -> Unit =
    { it: Routes -> applyToAll(it, action) }

data class EndpointBundle<T : Any>(
    val endpoint: Endpoint<T>,
    val response: EndpointResponse,
    val handlerResponse: HandlerResponse<Any, Any, out StatusCodes>,
    inline val handler: (RequestWrapper) -> HttpResponse<*, *>
) {
    val params = (endpoint.headerParams + endpoint.queryParams + endpoint.pathParams)
}

abstract class Sealed {
    val `$type`: String = this::class.simpleName!!
}

enum class Format(val type: String) {
    OctetStream("application/octet-stream"),
    Json("application/json"),
    ImageJpeg("image/jpeg"),
    VideoMP4("video/mp4"),
    TextHTML("text/html"),
    TextPlain("text/plain"),
}

interface Routed {
    val config: SnitchConfig
    val service: SnitchService
    val pathParams: Set<PathParam<out Any, out Any>>
    val endpoints: MutableList<EndpointBundle<*>>
    val parser: Parser
    val path: String
}

data class HandlerResponse<Request : Any, Response, S : StatusCodes>(
    val statusCodes: KType,
    val type: KType,
    val handler:
    context(Parser) TypedRequestWrapper<Request>.() -> HttpResponse<Response, S>
)

data class EndpointResponse(
    val statusCode: KType,
    val type: KType
)

sealed class StatusCodes(val code: Int = 200) {
    object CONTINUE : StatusCodes(100)
    object SWITCHING_PROTOCOLS : StatusCodes(101)
    object OK : StatusCodes(200)
    object CREATED : StatusCodes(201)
    object ACCEPTED : StatusCodes(202)
    object NON_AUTHORITATIVE_INFORMATION : StatusCodes(203)
    object NO_CONTENT : StatusCodes(204)
    object RESET_CONTENT : StatusCodes(205)
    object PARTIAL_CONTENT : StatusCodes(206)
    object MULTIPLE_CHOICES : StatusCodes(300)
    object MOVED_PERMANENTLY : StatusCodes(301)
    object FOUND : StatusCodes(302)
    object SEE_OTHER : StatusCodes(303)
    object NOT_MODIFIED : StatusCodes(304)
    object USE_PROXY : StatusCodes(305)
    object TEMPORARY_REDIRECT : StatusCodes(307)
    object BAD_REQUEST : StatusCodes(400)
    object UNAUTHORIZED : StatusCodes(401)
    object PAYMENT_REQUIRED : StatusCodes(402)
    object FORBIDDEN : StatusCodes(403)
    object NOT_FOUND : StatusCodes(404)
    object METHOD_NOT_ALLOWED : StatusCodes(405)
    object NOT_ACCEPTABLE : StatusCodes(406)
    object PROXY_AUTHENTICATION_REQUIRED : StatusCodes(407)
    object REQUEST_TIMEOUT : StatusCodes(408)
    object CONFLICT : StatusCodes(409)
    object GONE : StatusCodes(410)
    object LENGTH_REQUIRED : StatusCodes(411)
    object PRECONDITION_FAILED : StatusCodes(412)
    object PAYLOAD_TOO_LARGE : StatusCodes(413)
    object URI_TOO_LONG : StatusCodes(414)
    object UNSUPPORTED_MEDIA_TYPE : StatusCodes(415)
    object REQUESTED_RANGE_NOT_SATISFIABLE : StatusCodes(416)
    object EXPECTATION_FAILED : StatusCodes(417)
    object INTERNAL_SERVER_ERROR : StatusCodes(500)
    object NOT_IMPLEMENTED : StatusCodes(501)
    object BAD_GATEWAY : StatusCodes(502)
    object SERVICE_UNAVAILABLE : StatusCodes(503)
    object GATEWAY_TIMEOUT : StatusCodes(504)
    object HTTP_VERSION_NOT_SUPPORTED : StatusCodes(505)
}

enum class HTTPMethods {
    GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD;

    companion object {
        fun fromString(method: String) = when (method) {
            DELETE.name -> DELETE
            GET.name -> GET
            PUT.name -> PUT
            POST.name -> POST
            OPTIONS.name -> OPTIONS
            HEAD.name -> HEAD
            PATCH.name -> PATCH
            else -> throw IllegalArgumentException(method)
        }
    }
}

enum class ContentType(val value: String) {
    APPLICATION_ATOM_XML("application/atom+xml"),
    APPLICATION_FORM_URLENCODED("application/x-www-form-urlencoded"),
    APPLICATION_JSON("application/json"),
    APPLICATION_OCTET_STREAM("application/octet-stream"),
    APPLICATION_SVG_XML("application/svg+xml"),
    APPLICATION_XHTML_XML("application/xhtml+xml"),
    APPLICATION_XML("application/xml"),
    MULTIPART_FORM_DATA("multipart/form-data"),
    TEXT_HTML("text/html"),
    TEXT_PLAIN("text/plain"),
    TEXT_XML("text/xml"),
    WILDCARD("*/*");
}

data class ErrorResponse<E>(
    val statusCode: Int,
    val details: E,
)

data class Body<T : Any>(val klass: KClass<T>, val contentType: ContentType = ContentType.APPLICATION_JSON)

interface RequestWrapper : CommonResponses {
    val body: () -> Any?
    val params: Set<Parameter<*, *>>
    val parser: Parser
    val method: HTTPMethods
    val path: String
    val request get() = this

    fun params(name: String): String?
    fun headers(name: String): Collection<String>
    fun queryParams(name: String): Collection<String>
    fun getPathParam(param: PathParam<*, *>): String?
    fun getQueryParam(param: QueryParameter<*, *>): Collection<String>?
    fun getHeaderParam(param: HeaderParameter<*, *>): Collection<String>?

    private fun missingParameterMessage(path: String, it: Parameter<*, *>) =
        """Required $path parameter `${it.name}` is missing"""

    private fun invalidParameterMessage(query: String, it: Parameter<*, *>, value: Collection<String>?) =
        """$query parameter `${it.name}` is invalid, expecting ${it.pattern.description}, got `${value?.joinToString(",")}`"""

    fun getInvalidParams(): List<String> {
        return params
            .map {
                when (it) {
                    is PathParam<*, *> -> validateParam(it, listOf(getPathParam(it).orEmpty()), "Path")
                    is QueryParameter<*, *> -> validateParam(it, getQueryParam(it), "Query")
                    is HeaderParameter<*, *> -> validateParam(it, getHeaderParam(it), "Header")
                }
            }.filterNotNull().print()
    }

    private fun validateParam(it: Parameter<*, *>, value: Collection<String>?, path: String): String? {
        return try {
            when {
                it.required && (value == null || value.isEmpty()) -> missingParameterMessage(path, it)
                !it.required && value == null -> null
                value != null && it.pattern.parse(parser, value).let { true } -> null
                it.pattern.regex.matches(value.toString()) -> null
                else -> invalidParameterMessage(path, it, value)

            }
        } catch (e: Exception) {
            invalidParameterMessage(path, it, value)
        }
    }

    operator fun <T : Any, R> get(param: PathParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam {
                params(param.name)
                    .let { param.pattern.parse(parser, listOf(it.orEmpty())) }
            }

    operator fun <T : Any?, R> get(param: QueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam {
                queryParams(param.name)
                    .filterValid(param)
                    .let { param.pattern.parse(parser, it.orEmpty()) }
            }

    operator fun <T : Any?, R> get(param: OptionalQueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam {
                queryParams(param.name)
                    .filterValid(param)
                    ?.let { param.pattern.parse(parser, it) } ?: param.default
            }

    operator fun <T : Any?, R> get(param: HeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam {
                headers(param.name)
                    .let { param.pattern.parse(parser, it.orEmpty()) }
            }


    operator fun <T : Any?, R> get(param: OptionalHeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam {
                headers(param.name)
                    .filterValid(param)
                    ?.let { param.pattern.parse(parser, it) } ?: param.default
            }
}

private inline fun RequestWrapper.checkParamIsRegistered(param: Parameter<*, *>) =
    if (!params.contains(param)) throw UnregisteredParamException(param) else this

private inline fun <R> RequestWrapper.tryParam(block: () -> R) = try {
    block()
} catch (e: Exception) {
    throw InvalidParametersException(e, getInvalidParams())
}

fun Collection<String>.filterValid(param: Parameter<*, *>): Collection<String>? = when {
    this.isEmpty() -> null
    param.emptyAsMissing && this.all { it.isEmpty()} -> null
    param.invalidAsMissing -> null
    else -> this
}

fun String?.filterValid(param: Parameter<*, *>): String? = when {
    this == null -> null
    param.emptyAsMissing && this.isEmpty() -> null
    param.invalidAsMissing -> null
    else -> this
}

@JvmInline
value class TypedRequestWrapper<T : Any>(
    val request: RequestWrapper,
): CommonResponses {
    val body: T get() = request.body() as T
}

class Handler<Request : Any, Response, S : StatusCodes>(val block: context(Parser) TypedRequestWrapper<Request>.() -> HttpResponse<Response, S>) {
    operator fun getValue(
        nothing: Nothing?,
        property: KProperty<*>
    ): HandlerResponse<Request, Response, S> {
        val type = property.returnType.arguments.get(1).type
        val statusCode = property.returnType.arguments.get(2).type

        return HandlerResponse(statusCode!!, type!!, block)
    }
}

class BodiedHandler<B: Any> {
    infix fun <T, S: StatusCodes> handling(block: context(Parser) TypedRequestWrapper<B>.() -> HttpResponse<T, S>) = Handler<B, T,S>(block)
    infix fun <T, S: StatusCodes> thenHandling(block: context(Parser) TypedRequestWrapper<B>.() -> HttpResponse<T, S>) = Handler<B, T,S>(block)
}

fun <B: Any, T, S: StatusCodes> handling(b: KClass<B>, block: context(Parser) TypedRequestWrapper<B>.() -> HttpResponse<T, S>) = Handler<B, T,S>(block)

fun <B: Any, T, S: StatusCodes> handling(b: Function<B>, block: context(Parser) TypedRequestWrapper<B>.() -> HttpResponse<T, S>) = Handler<B, T,S>(block)

fun <T, S: StatusCodes> handling(block: context(Parser) TypedRequestWrapper<Nothing>.() -> HttpResponse<T, S>) = Handler<Nothing, T,S>(block)

inline fun <reified B: Any> parsing() = BodiedHandler<B>()

fun noBody() = BodiedHandler<Nothing>()

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

internal fun OpenApi.withPath(name: String, path: Path) =
    copy(paths = paths + (name to path))

internal fun Path.withOperation(method: HTTPMethods, operation: Operation) = when (method) {
    HTTPMethods.GET -> copy(get = operation)
    HTTPMethods.POST -> copy(post = operation)
    HTTPMethods.DELETE -> copy(delete = operation)
    HTTPMethods.PUT -> copy(put = operation)
    HTTPMethods.PATCH -> copy(patch = operation)
    HTTPMethods.HEAD -> copy(head = operation)
    HTTPMethods.OPTIONS -> copy(options = operation)
}

internal fun Operation.withParameter(parameter: Parameters.Parameter) =
    copy(parameters = (parameters ?: emptyList()) + parameter)

internal fun Operation.withRequestBody(
    documentationSerializer: DocumentationSerializer,
    contentType: ContentType,
    body: KClass<*>
) =
    copy(
        requestBody = RequestBodies.RequestBody(
            content =
            mapOf(contentType.value to MediaType(toSchema(documentationSerializer, body.starProjectedType)))
        )
    )

internal fun Operation.withResponse(
    documentationSerializer: DocumentationSerializer,
    contentType: ContentType,
    body: KType,
    code: String
) = copy(
    responses = responses + (code to Responses.Response(
        content = mapOf(
            contentType.value to MediaType(
                toSchema(documentationSerializer, body)
            )
        )
    ))
)

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

data class DocumentedService(val service: RoutedService, val documentation: Spec)

fun RoutedService.generateDocumentation(
    documentationSerializer: DocumentationSerializer = DefaultDocumentatoinSerializer,
    documentationConfig: DocumentationConfig = DocumentationConfig(
        port = this.service.config.service.port,
        basePath = this.service.config.service.basePath
    )
): DocumentedService {
    val openApi =
        OpenApi(info = Info(documentationConfig.title, "1.0"), servers = listOf(Server(documentationConfig.host)))
    return router.endpoints
        .groupBy { it.endpoint.url }
        .map { entry ->
            entry.key to entry.value.foldRight(Path()) { bundle: EndpointBundle<*>, path ->
                path.withOperation(
                    bundle.endpoint.httpMethod,
                    Operation(
                        tags = bundle.endpoint.tags,
                        summary = bundle.endpoint.summary,
                        description = bundle.endpoint.description,
                        responses = emptyMap(),
                        visibility = bundle.endpoint.visibility
                    )
                        .withResponse(
                            documentationSerializer,
                            if (bundle.response.type.jvmErasure == ByteArray::class) {
                                ContentType.APPLICATION_OCTET_STREAM
                            } else {
                                ContentType.APPLICATION_JSON
                            },
                            bundle.response.type,
                            (bundle.response.statusCode.jvmErasure.objectInstance as? StatusCodes)?.code?.toString()
                                ?: "200"
                        )
                        .let {
                            if (bundle.endpoint.body.klass != Nothing::class) {
                                it.withRequestBody(
                                    documentationSerializer,
                                    bundle.endpoint.body.contentType,
                                    bundle.endpoint.body.klass
                                )
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
        .let {
            DocumentedService(this, Spec(with(router.parser) { it.serialized }, router))
        }
}

class Spec internal constructor(
    val spec: String,
    val router: Router,
)

fun DocumentedService.servePublicDocumenation(): DocumentedService {
    with(Router(service.router.config, service.service, emptySet(), service.router.parser, service.router.path)) {
        val path = "/"// config.publicDocumentationPath.ensureLeadingSlash()
        val docPath = "/spec.json"//.ensureLeadingSlash()
        GET(path).isHandledBy {
            index(docPath).ok.format(TextHTML)
        }
        GET(docPath).isHandledBy {
            documentation.spec.ok.format(Json).serializer { it }
        }
        endpoints.forEach { service.registerMethod(it, it.endpoint.url) }
    }
    return this
}

fun RoutedService.serveDocumentation() =
    generateDocumentation().servePublicDocumenation()

private fun getDescription(param: Parameter<*, *>) =
    "${param.description} - ${param.pattern.description}${if (param.invalidAsMissing) " - Invalid as Missing" else ""}${if (param.emptyAsMissing) " - Empty as Missing" else ""}"

fun index(docPath: String) = """
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
        url: ".$docPath",
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

internal fun toSchema(documentationSerializer: DocumentationSerializer, type: KType): Schemas {
    val klass = type.jvmErasure
    val schema = when {
        klass == String::class -> Schemas.StringSchema()
        klass == Int::class -> Schemas.IntSchema()
        klass == Long::class -> Schemas.LongSchema()
        klass == Float::class -> Schemas.FloatSchema()
        klass == Double::class -> Schemas.DoubleSchema()
        klass == Boolean::class -> Schemas.BooleanSchema()
        klass == Date::class -> Schemas.DateSchema()
        klass == ByteArray::class -> Schemas.BinarySchema()
        klass == List::class -> Schemas.ArraySchema(items = toSchema(documentationSerializer, type.arguments.first().type!!))
        klass.java.isEnum -> Schemas.StringSchema(enum = klass.java.enumConstants.map { it.toString() })
        klass.isSealed && Sealed::class.java.isAssignableFrom(klass.java) -> sealedSchema(documentationSerializer, klass, type)
        klass.objectInstance != null -> Schemas.ObjectSchema()
        klass.primaryConstructor == null -> Schemas.StringSchema()
        else -> objectSchema(documentationSerializer, type)
    }
    return schema.apply {
        if (type.isMarkedNullable) nullable = true
    }
}

private fun objectSchema(serializer: DocumentationSerializer, type: KType): Schemas.ObjectSchema {
    val parameters: List<KParameter> = type.jvmErasure.primaryConstructor!!.parameters
    return Schemas.ObjectSchema(
        properties = parameters.map { param ->
            val paramType = if (param.type.jvmErasure.java == Object::class.java) {
                type.arguments.singleOrNull()?.type
            } else null
            serializer.serializeField(param, type.jvmErasure) to (toSchema(serializer, paramType ?: param.type)
                .let { schema ->
                    val desc = param.annotations.find { it is Description }?.let { (it as Description) }
                    when (schema) {
                        is Schemas.ArraySchema -> schema.apply {
                            description = desc?.description
                            example = when {
                                desc?.exEmptyList != null && desc.exEmptyList -> listOf<Any>()
                                else -> null
                            }
                            pattern = desc?.pattern
                            visibility = desc?.visibility
                        }

                        is Schemas.StringSchema -> schema.apply {
                            description = desc?.description
                            example = desc?.exString?.nullIfEmpty()
                            pattern = desc?.pattern
                            visibility = desc?.visibility
                        }

                        is Schemas.FloatSchema -> schema.apply {
                            description = desc?.description
                            example = desc?.exFloat?.nullIfZero()
                            pattern = desc?.pattern
                            visibility = desc?.visibility
                        }

                        is Schemas.DoubleSchema -> schema.apply {
                            description = desc?.description
                            example = desc?.exDouble?.nullIfZero()
                            pattern = desc?.pattern
                            visibility = desc?.visibility
                        }

                        is Schemas.IntSchema -> schema.apply {
                            description = desc?.description
                            example = desc?.exInt?.nullIfZero()
                            pattern = desc?.pattern
                            visibility = desc?.visibility
                        }

                        is Schemas.LongSchema -> schema.apply {
                            description = desc?.description
                            example = desc?.exLong?.nullIfZero()
                            pattern = desc?.pattern
                            visibility = desc?.visibility
                        }

                        is Schemas.BaseSchema<*> -> schema.apply {
                            description = desc?.description
                            pattern = desc?.pattern
                            visibility = desc?.visibility
                        }

                        else -> schema
                    }
                }
                    )
        }
            .toMap(),
        required = parameters.filter { !it.type.isMarkedNullable }.map { it.name!! }
    )
}

private fun sealedSchema(
    documentationSerializer: DocumentationSerializer,
    klass: KClass<*>,
    type: KType
): Schemas.ObjectSchema {
    val subtypes = klass.nestedClasses.filter { it.isFinal && Sealed::class.java.isAssignableFrom(it.java) }
        .map { it.starProjectedType }
    return Schemas.ObjectSchema(anyOf = subtypes.map { o ->
        toSchema(documentationSerializer, o).let {
            when (it) {
                is Schemas.ObjectSchema -> {
                    it.copy(
                        properties = mapOf(Sealed::`$type`.name to Schemas.StringSchema(description = o.jvmErasure.simpleName)) + (it.properties
                            ?: emptyMap()),
                        required = listOf(Sealed::`$type`.name) + (it.required ?: emptyList())
                    )
                }

                else -> it
            }
        }
    }, example = getExample(type))
}

private fun String.nullIfEmpty() = if (isEmpty()) null else this

private fun Int.nullIfZero() = if (this == 0) null else this

private fun Long.nullIfZero() = if (this == 0L) null else this

private fun Double.nullIfZero() = if (this == 0.0) null else this

private fun Float.nullIfZero() = if (this == 0f) null else this

@Suppress("IMPLICIT_CAST_TO_ANY")
private fun getExample(type: KType): Any {
    val klass = type.jvmErasure
    val value = when {
        klass == String::class -> "string"
        klass == Int::class -> 0
        klass == Long::class -> 0
        klass == Float::class -> 0.0f
        klass == Double::class -> 0.0
        klass == Boolean::class -> false
        klass == ByteArray::class -> listOf<String>()
        klass == Date::class -> Date().toString()
        klass == List::class -> listOf(getExample(type.arguments.first().type!!))
        klass.java.isEnum -> klass.java.enumConstants.map { it.toString() }.first()
        klass.objectInstance != null && Sealed::class.java.isAssignableFrom(klass.java) -> mapOf(Sealed::`$type`.name to klass.simpleName)
        klass.isSealed && Sealed::class.java.isAssignableFrom(klass.java) -> {
            val subclass =
                klass.nestedClasses.filter { it.isFinal && Sealed::class.java.isAssignableFrom(it.java) }.first()
            val ex = getExample(subclass.starProjectedType) as Map<Any, Any>
            mapOf(Sealed::`$type`.name to subclass.simpleName) + ex
        }

        else -> {
            val parameters = klass.primaryConstructor!!.parameters
            parameters.map { it.name!! to getExample(it.type) }.toMap()
        }
    }
    return value
}

interface DocumentationSerializer {
    fun serializeField(param: KParameter, klass: KClass<*>): String
}

object DefaultDocumentatoinSerializer : DocumentationSerializer {
    override fun serializeField(param: KParameter, klass: KClass<*>): String = param.name!!
}

data class UnregisteredParamException(val param: Parameter<*, *>) : Exception()

val ofNonNegativeInt = validator<Int, Int>(
    "non negative integer",
    """^\d+$""".toRegex()
) {
    it.toInt().also {
        if (it < 0) throw IllegalArgumentException()
    }
}

val ofNonEmptyString = stringValidator("non empty string") {
    if (it.isEmpty()) throw IllegalArgumentException()
    else it
}

val ofNonEmptySingleLineString = stringValidator("non empty single-line string") {
    if (it.isEmpty() || it.lines().size != 1)
        throw IllegalArgumentException()
    else it
}

val ofNonEmptyStringSet = stringValidator("non empty string set") {
    it.split(",").toSet()
}

val ofStringSet = stringValidatorMulti("string set") {
    it.flatMap { it.split(",") }.toSet()
}

class Enum<E : kotlin.Enum<*>>(e: KClass<E>) : Validator<E, E> {
    private val values = e.java.enumConstants.asList().joinToString("|")
    override val description: String = "A string of value: $values"
    override val parse: Parser.(Collection<String>) -> E = {
        it.firstOrNull()!!.let {
            it.parse(e.java)
        }
    }
    override val regex: Regex = "^($values)$".toRegex()
}

inline fun <reified E : kotlin.Enum<*>> ofEnum(): Validator<String, E> {
    val e = E::class
    val values = e.java.enumConstants.asList().joinToString("|")
    val regex: Regex = "^($values)$".toRegex()
    val description: String = "A string of value: $values"
    return validator(description, regex) {
        it.parse(e.java)
    }
}

inline fun <reified E : kotlin.Enum<*>> ofEnumMulti(): Validator<String, Collection<E>> {
    val e = E::class
    val values = e.java.enumConstants.asList().joinToString("|")
    val regex: Regex = "^($values)$".toRegex()
    val description: String = "A string of value: $values"
    return validatorMulti(description, regex) {
        it.map { it.parse(e.java) }
    }
}

interface Validator<T, R> {
    val regex: Regex
    val description: String
    val parse: Parser.(Collection<String>) -> R
    fun optional(): Validator<T?, R?> = this as Validator<T?, R?>
}

inline fun <From, To> validator(
    descriptions: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    crossinline mapper: Parser.(String) -> To
) = object : Validator<From, To> {
    override val description = descriptions
    override val regex = regex
    override val parse: Parser.(Collection<String>) -> To = { mapper(it.single()) }
}

inline fun <To> stringValidator(
    description: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    crossinline mapper: Parser.(String) -> To,
) = validator<String, To>(description, regex, mapper)

fun <From, To> validatorMulti(
    descriptions: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    mapper: Parser.(Collection<String>) -> To
) = object : Validator<From, To> {
    override val description = descriptions
    override val regex = regex
    override val parse: Parser.(Collection<String>) -> To = mapper
}

fun <To> stringValidatorMulti(
    description: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    mapper: Parser.(Collection<String>) -> To,
) = validatorMulti<String, To>(description, regex, mapper)

inline fun <reified T, R> optionalHeader(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) =
    OptionalHeaderParamDelegate(
        type = T::class.java,
        name = name,
        pattern = condition.optional(),
        description = description,
        default = null,
        emptyAsMissing = emptyAsMissing,
        invalidAsMissing = invalidAsMissing,
        visibility = visibility
    )

fun optionalHeader(
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = optionalHeader(
    condition = ofNonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

fun optionalHeader(
    name: String = "",
    default: String,
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = optionalHeader(
    condition = ofNonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    default = default,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> optionalHeader(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    default: R,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = OptionalHeaderParamDelegate(
    type = T::class.java,
    name = name,
    pattern = condition,
    description = description,
    default = default,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

fun header(
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = header(
    condition = ofNonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> header(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = HeaderParamDelegate(
    type = T::class.java,
    name = name,
    pattern = condition,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> path(condition: Validator<T, R>, name: String = "", description: String = "") =
    PathParamDelegate(
        type = T::class.java,
        name = name,
        pattern = condition,
        description = description
    )

fun path(name: String = "", description: String = "") = path(ofNonEmptyString, name, description)

class InvalidParametersException(
    val e: Throwable,
    val reasons: List<String>): Exception(e)

data class PathParam<T, R>(
    val path: String? = null,
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String
) : Parameter<T, R>(type, name, pattern, description, true, false) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PathParam<*, *>

        if (path != other.path) return false
        if (type != other.type) return false
        if (name != other.name) return false
        return pattern == other.pattern
    }

    override fun hashCode(): Int {
        var result = path?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + pattern.hashCode()
        return result
    }
}

class PathParamDelegate<T, R>(
    val type: Class<*>,
    val name: String,
    val pattern: Validator<T, R>,
    val description: String,
) {
    private var param: PathParam<T, R>? = null
    operator fun getValue(nothing: Nothing?, property: KProperty<*>): PathParam<T, R> = param(property)
    operator fun getValue(nothing: Any?, property: KProperty<*>): PathParam<T, R> = param(property)

    private fun param(property: KProperty<*>) =
        param ?: PathParam(
            "{${name.ifEmpty { property.name }}}",
            type,
            name.ifEmpty { property.name },
            pattern,
            description,
        ).also { param = it }
}

data class ParametrizedPath(val path: String, val pathParameters: Set<PathParam<out Any, out Any>>) {
    operator fun div(path: String) = copy(path = this.path + "/" + path)
    operator fun div(path: PathParam<out Any, out Any>) =
        copy(path = this.path + "/" + "{${path.name}}", pathParameters = pathParameters + path)
}

sealed class Parameter<T, R>(
    open val type: Class<*>,
    open val name: String,
    open val pattern: Validator<T, R>,
    open val description: String,
    open val required: Boolean = false,
    open val emptyAsMissing: Boolean = false,
    open val invalidAsMissing: Boolean = false
)

sealed class QueryParameter<T, R>(
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    override val required: Boolean = false,
    override val emptyAsMissing: Boolean = false,
    override val invalidAsMissing: Boolean = false,
    open val visibility: Visibility = Visibility.PUBLIC
) : Parameter<T, R>(type, name, pattern, description, required, emptyAsMissing)

data class OptionalQueryParam<T, R>(
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    val default: R,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: Visibility
) : QueryParameter<T, R>(type, name, pattern, description, false, emptyAsMissing, invalidAsMissing)

data class QueryParam<T, R>(
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: Visibility
) : QueryParameter<T, R>(type, name, pattern, description, true, emptyAsMissing, invalidAsMissing)

class QueryParamDelegate<T, R>(
    val type: Class<*>,
    val name: String,
    val pattern: Validator<T, R>,
    val description: String,
    val emptyAsMissing: Boolean,
    val invalidAsMissing: Boolean,
    val visibility: Visibility
) {
    var param: QueryParam<T, R>? = null
    operator fun getValue(nothing: Nothing?, property: KProperty<*>) = param(property)
    operator fun getValue(nothing: Any?, property: KProperty<*>) = param(property)

    private fun param(property: KProperty<*>) =
        param ?: QueryParam(
            type,
            name.ifEmpty { property.name },
            pattern,
            description,
            emptyAsMissing,
            invalidAsMissing,
            visibility
        ).also { param = it }
}

class OptionalQueryParamDelegate<T, R>(
    val type: Class<*>,
    val name: String,
    val pattern: Validator<T, R>,
    val description: String,
    val default: R,
    val emptyAsMissing: Boolean,
    val invalidAsMissing: Boolean,
    val visibility: Visibility
) {
    private var param: OptionalQueryParam<T, R>? = null
    operator fun getValue(nothing: Nothing?, property: KProperty<*>) = param(property)
    operator fun getValue(nothing: Any?, property: KProperty<*>) = param(property)

    private fun param(property: KProperty<*>) =
        param ?: OptionalQueryParam(
            type,
            name.ifEmpty { property.name },
            pattern,
            description,
            default,
            emptyAsMissing,
            invalidAsMissing,
            visibility
        ).also { param = it }
}

sealed class HeaderParameter<T, R>(
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    override val required: Boolean = false,
    override val emptyAsMissing: Boolean = false,
    override val invalidAsMissing: Boolean = false,
    open val visibility: Visibility = Visibility.PUBLIC
) : Parameter<T, R>(type, name, pattern, description, required, emptyAsMissing)

data class HeaderParam<T, R>(
    override val type: Class<*>,
    override val name: String,
    override inline val pattern: Validator<T, R>,
    override val description: String,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: Visibility
) : HeaderParameter<T, R>(type, name, pattern, description, true, emptyAsMissing, invalidAsMissing)

data class OptionalHeaderParam<T, R>(
    override val type: Class<*>,
    override inline val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    val default: R,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: Visibility
) : HeaderParameter<T, R>(type, name, pattern, description, false, emptyAsMissing, invalidAsMissing)

class HeaderParamDelegate<T, R>(
    val type: Class<*>,
    val name: String,
    val pattern: Validator<T, R>,
    val description: String,
    val emptyAsMissing: Boolean,
    val invalidAsMissing: Boolean,
    val visibility: Visibility
) {
    private var param: HeaderParam<T, R>? = null
    operator fun getValue(nothing: Nothing?, property: KProperty<*>) = param(property)
    operator fun getValue(nothing: Any?, property: KProperty<*>) = param(property)

    private fun param(property: KProperty<*>) =
        param ?: HeaderParam(
            type,
            name.ifEmpty { property.name },
            pattern,
            description,
            emptyAsMissing,
            invalidAsMissing,
            visibility
        ).also { param = it }
}

class OptionalHeaderParamDelegate<T, R>(
    val type: Class<*>,
    val name: String,
    val pattern: Validator<T, R>,
    val description: String,
    val default: R,
    val emptyAsMissing: Boolean,
    val invalidAsMissing: Boolean,
    val visibility: Visibility
) {
    var param: OptionalHeaderParam<T, R>? = null
    operator fun getValue(nothing: Nothing?, property: KProperty<*>) = param(property)
    operator fun getValue(nothing: Any?, property: KProperty<*>) = param(property)

    private fun param(property: KProperty<*>) =
        param ?: OptionalHeaderParam(
            type,
            name.ifEmpty { property.name },
            pattern,
            description,
            default,
            emptyAsMissing,
            invalidAsMissing,
            visibility
        ).also { param = it }
}

inline fun <reified T, R> optionalQuery(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = OptionalQueryParamDelegate(
    type = T::class.java,
    name = name,
    pattern = condition.optional(),
    description = description,
    default = null,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> optionalQuery(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    default: R,
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = OptionalQueryParamDelegate(
    type = T::class.java,
    name = name,
    pattern = condition,
    description = description,
    default = default,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

fun optionalQuery(
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = optionalQuery(
    condition = ofNonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

fun optionalQuery(
    name: String = "",
    default: String,
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = optionalQuery(
    condition = ofNonEmptyString,
    name = name,
    description = description,
    default = default,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

fun query(
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = query(
    condition = ofNonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> query(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = QueryParamDelegate(
    type = T::class.java,
    name = name,
    pattern = condition,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

sealed class HttpResponse<T, out S : StatusCodes> {
    abstract val statusCode: StatusCodes
    abstract val headers: Map<String, String>
    abstract val value: context(Parser) () -> Any?

    fun map(
        failure: ErrorHttpResponse<T, *, S>.() -> HttpResponse<Any, *> = { this as HttpResponse<Any, *> },
        success: SuccessfulHttpResponse<T, S>.() -> HttpResponse<Any, *>,
    ): HttpResponse<Any, *> = when (this) {
        is SuccessfulHttpResponse -> this.success()
        is ErrorHttpResponse<T, *, S> -> this.failure()
    }
}

data class SuccessfulHttpResponse<T, out S : StatusCodes>(
    override val statusCode: S,
    val body: T,
    val _format: Format = Json,
    override inline val value: context(Parser) () -> Any? = {
        when (_format) {
            Json -> body?.serialized
            TextHTML -> body
            OctetStream -> body
            TextPlain -> body
            ImageJpeg -> body
            VideoMP4 -> body
        }
    },
    override val headers: Map<String, String> = emptyMap(),
) : HttpResponse<T, S>()

data class ErrorHttpResponse<T, E, out S : StatusCodes>(
    override val statusCode: StatusCodes,
    val details: E,
    override val value: context(Parser) () -> Any? = { details?.serialized },
    override val headers: Map<String, String> = emptyMap(),
) : HttpResponse<T, S>()

interface CommonResponses {
    fun <T, S : StatusCodes> HttpResponse<T, S>.format(newFormat: Format) =
        if (this is SuccessfulHttpResponse<T, S>) copy(_format = newFormat) else this

    fun <T, S : StatusCodes> HttpResponse<T, S>.serializer(serializer: (T) -> Any) =
        if (this is SuccessfulHttpResponse<T, S>) copy(value = { serializer(this.body) }) else this

    val <T> T.ok
        get() = SuccessfulHttpResponse(StatusCodes.OK, this)

    val <T> T.created: SuccessfulHttpResponse<T, StatusCodes.CREATED>
        get() = SuccessfulHttpResponse(StatusCodes.CREATED, this)

    val <T> T.accepted
        get() = SuccessfulHttpResponse(StatusCodes.ACCEPTED, this)

    val <T> T.noContent get() = SuccessfulHttpResponse(StatusCodes.NO_CONTENT, this)

    fun <T, E, S: StatusCodes> E.badRequest() = ErrorHttpResponse<T, _, S>(StatusCodes.BAD_REQUEST, this)

    fun <T, E, S: StatusCodes> E.unauthorized() = ErrorHttpResponse<T, _,S>(StatusCodes.UNAUTHORIZED, this)

    fun <T, E, S: StatusCodes> E.forbidden() = ErrorHttpResponse<T, _, S>(StatusCodes.FORBIDDEN, this)

    fun <T, E, S: StatusCodes> E.notFound() = ErrorHttpResponse<T, _, S>(StatusCodes.NOT_FOUND, this)

    fun <T, E, S: StatusCodes> E.serverError() = ErrorHttpResponse<T, _, S>(StatusCodes.INTERNAL_SERVER_ERROR, this)
}

interface SnitchService {
    val config: SnitchConfig
    fun registerMethod(endpointBundle: EndpointBundle<*>, path: String)
    fun onRoutes(routerConfiguration: Routes): RoutedService
    fun onStop(action: () -> Unit): SnitchService

    fun <T : Throwable, R : HttpResponse<*, *>> handleException(
        exceptionClass: KClass<T>,
        exceptionHandler: context(Parser) RequestWrapper.(T) -> R
    )
}

data class RoutedService(
    val service: SnitchService,
    val router: Router,
    private val onStart: () -> Unit,
    private val onStop: () -> Unit,
) {
    fun start(): RoutedService {
        router.endpoints.forEach {
            val path: String = service.config.service.basePath + it.endpoint.url
            service.registerMethod(it, path)
        }

        onStart()
        return this
    }

    fun stop() {
        onStop()
    }

    inline fun <reified T : Throwable, R : HttpResponse<Any, StatusCodes>> handleException(noinline block: context(Parser) RequestWrapper.(T) -> R): RoutedService {
        service.handleException(T::class, block)
        return this
    }

    fun <T : Throwable, R : HttpResponse<Any, StatusCodes>> handleException(ex: KClass<T>, block: context(Parser) RequestWrapper.(T) -> R): RoutedService {
        service.handleException(ex, block)
        return this
    }
}

class DecoratedWrapper(
    val next: () -> HttpResponse<out Any, *>,
    val wrap: RequestWrapper
): RequestWrapper by wrap

fun RoutedService.handleInvalidParameters() =
    handleException<InvalidParametersException, _> { ex ->
        ErrorResponse(400, ex.reasons).badRequest()
    }

fun RoutedService.handleUnregisteredParameters() =
    handleException<UnregisteredParamException, _> { ex ->
        val type = when (ex.param) {
            is HeaderParameter -> "header"
            is QueryParameter -> "query"
            is PathParam -> "path"
        }

        ErrorResponse(
            500,
            "Attempting to use unregistered $type parameter `${ex.param.name}`"
        ).serverError()
    }

fun RoutedService.handleParsingException() =
    handleException<ParsingException, _> { ex ->
        ErrorResponse(400, "Invalid body parameter").badRequest()
    }

data class OpDescription(val description: String)

data class Endpoint<B : Any>(
    val httpMethod: HTTPMethods,
    val summary: String?,
    val description: String?,
    val url: String,
    val pathParams: Set<PathParam<out Any, *>>,
    val queryParams: Set<QueryParameter<*, *>>,
    val headerParams: Set<HeaderParameter<*, *>>,
    val body: Body<B>,
    val tags: List<String>? = emptyList(),
    val visibility: Visibility = Visibility.PUBLIC,
    val decorator: DecoratedWrapper.() -> DecoratedWrapper = { this },
) {
    infix fun decorate(decoration: DecoratedWrapper.() -> HttpResponse<out Any, StatusCodes>): Endpoint<B> {
        val previousDecorator = this.decorator
        return copy(
            decorator = { DecoratedWrapper({ decoration(previousDecorator(this)) }, wrap) }
        )
    }

    infix fun withQuery(queryParameter: QueryParameter<*, *>) = copy(queryParams = queryParams + queryParameter)

    infix fun withHeader(params: HeaderParameter<*, *>) = copy(headerParams = headerParams + params)
    infix fun <C : Any> with(body: Body<C>) = Endpoint(
        httpMethod,
        summary,
        description,
        url,
        pathParams,
        queryParams,
        headerParams,
        body,
        tags,
        visibility,
        decorator,
    )

    infix fun inSummary(summary: String) = copy(summary = summary)

    infix fun isDescribedAs(description: String) = copy(description = description)

    infix fun with(visibility: Visibility) = copy(visibility = visibility)

    infix fun with(queryParameter: List<Parameter<*, *>>) = let {
        queryParameter.foldRight(this) { param, endpoint ->
            when (param) {
                is HeaderParameter -> endpoint withHeader param
                is QueryParameter -> endpoint withQuery param
                else -> throw IllegalArgumentException(param.toString())
            }
        }
    }

    infix fun doBefore(action: BeforeAction) = decorate {
        action(wrap)
        next()
    }

    infix fun doAfter(action: AfterAction) = decorate {
        next().also { action(wrap, it) }
    }

    infix fun onlyIf(condition: Condition) = decorate {
        when (val result = condition.check(wrap)) {
            is ConditionResult.Successful -> next()
            is ConditionResult.Failed -> result.response
        }
    }
}

interface Condition {
    fun check(requestWrapper: RequestWrapper): ConditionResult
    infix fun or(other: Condition): Condition = OrCondition(this, other)
}

class OrCondition(private val first: Condition, private val second: Condition) : Condition {
    override fun check(requestWrapper: RequestWrapper) =
        when (val result = first.check(requestWrapper)) {
            is ConditionResult.Successful -> result
            is ConditionResult.Failed -> second.check(requestWrapper)
        }
}

sealed class ConditionResult {
    class Successful : ConditionResult()
    data class Failed(val response: ErrorHttpResponse<Any, out Any, StatusCodes.BAD_REQUEST>) : ConditionResult()
}

interface Parser {
    val Any.serialized: String
    val Any.serializedBytes: ByteArray

    fun <T: Any> String.parse(klass: Class<T>): T
    fun <T: Any> ByteArray.parse(klass: Class<T>): T
}

class ParsingException(exception: Exception): Exception(exception)

fun <T : Any?> T.print(): T = this.apply {
    val stackFrame = Thread.currentThread().stackTrace[2]
    val className = stackFrame.className
    val methodName = stackFrame.methodName
    val fileName = stackFrame.fileName
    val lineNumber = stackFrame.lineNumber
    println("$this at $className.$methodName($fileName:$lineNumber)")
}

