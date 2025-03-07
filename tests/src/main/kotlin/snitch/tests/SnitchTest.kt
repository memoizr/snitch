package snitch.tests

import com.google.gson.JsonElement
import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import snitch.parsers.GsonJsonParser
import snitch.parsers.GsonJsonParser.parse
import snitch.parsers.GsonJsonParser.serialized
import snitch.service.RoutedService
import java.net.BindException
import java.net.ConnectException
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*

private fun <T> retry(block: () -> T): T {
    fun go(): T {
        try {
            return block()
        } catch (b: BindException) {
            return go()
        } catch (e: ConnectException) {
            return go()
        }
    }
    return go()
}

interface Ported {
    val port: Int
}

interface TestMethods : Ported {
    val httpClient: HttpClient get() = DefaultHttpClient
    
    infix fun GET(endpoint: String): Expectation {
        return Expectation(port, HttpMethod.GET, endpoint, httpClient)
    }

    infix fun POST(endpoint: String): Expectation {
        return Expectation(port, HttpMethod.POST, endpoint, httpClient)
    }

    infix fun DELETE(endpoint: String): Expectation {
        return Expectation(port, HttpMethod.DELETE, endpoint, httpClient)
    }

    infix fun PUT(endpoint: String): Expectation {
        return Expectation(port, HttpMethod.PUT, endpoint, httpClient)
    }

    infix fun PATCH(endpoint: String): Expectation {
        return Expectation(port, HttpMethod.PATCH, endpoint, httpClient)
    }
}

interface HttpClient {
    fun get(url: String, headers: Map<String, String>): HttpResponse<String>
    fun post(url: String, headers: Map<String, String>, body: Any?): HttpResponse<String>
    fun put(url: String, headers: Map<String, String>, body: Any?): HttpResponse<String>
    fun patch(url: String, headers: Map<String, String>, body: Any?): HttpResponse<String>
    fun delete(url: String, headers: Map<String, String>): HttpResponse<String>
}

object DefaultHttpClient : HttpClient {
    private fun call(
        url: String,
        headers: Map<String, String>,
        fn: HttpRequest.Builder.() -> HttpRequest.Builder
    ) = retry {
        clnt.send(
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .apply {
                    headers.forEach {
                        setHeader(it.key, it.value)
                    }
                }
                .fn()
                .build(), HttpResponse.BodyHandlers.ofString()
        )
    }

    override fun get(url: String, headers: Map<String, String>) = call(url, headers) { GET() }

    override fun post(url: String, headers: Map<String, String>, body: Any?) =
        call(url, headers) { POST(getBodyPublisher(body)) }

    override fun put(url: String, headers: Map<String, String>, body: Any?) =
        call(url, headers) { PUT(getBodyPublisher(body)) }

    override fun patch(url: String, headers: Map<String, String>, body: Any?) =
        call(url, headers) { method("PATCH", getBodyPublisher(body)) }

    override fun delete(url: String, headers: Map<String, String>) = call(url, headers) { DELETE() }

    private fun getBodyPublisher(body: Any?) = when (body) {
        is String? -> HttpRequest.BodyPublishers.ofString(body.orEmpty())
        is ByteArray? -> HttpRequest.BodyPublishers.ofByteArray(body ?: byteArrayOf())
        else -> HttpRequest.BodyPublishers.ofString(body?.serialized.orEmpty())
    }
}

enum class HttpMethod {
    POST, GET, PUT, DELETE, PATCH;
}

data class Expectation(
    val port: Int,
    private val method: HttpMethod,
    private val endpoint: String,
    private val client: HttpClient,
    private val headers: Map<String, String> = emptyMap(),
    private val body: Any? = null
) {
    val response: HttpResponse<String> by lazy {
        with(GsonJsonParser) {
            when (method) {
                HttpMethod.GET -> client.get("http://localhost:${port}$endpoint", headers)
                HttpMethod.PUT -> client.put(
                    "http://localhost:${port}$endpoint",
                    headers,
                    body
                )
                HttpMethod.POST -> client.post(
                    "http://localhost:${port}$endpoint",
                    headers,
                    body
                )
                HttpMethod.DELETE -> client.delete("http://localhost:${port}$endpoint", headers)
                HttpMethod.PATCH -> client.patch(
                    "http://localhost:${port}$endpoint", 
                    headers,
                    body
                )
            }
        }
    }

    infix fun withBody(body: Any) = copy(body = body)

    infix fun withHeaders(headers: Map<String, Any?>) =
        copy(headers = headers.map { it.key to it.value.toString() }.toMap())

    infix fun withHeader(header: Pair<String, String>) =
        copy(headers = headers + header)

    infix fun expectBody(body: String) = apply {
        val actual = response.body()
        assert(actual == body, {
            """Body assertion failed.
                | Expected: $body
                | But got: $actual
            """.trimMargin()
        })
    }

    infix fun expectCode(expected: Int) = apply {
        val actual = response.statusCode()
        assert(actual == expected)
        { "Expecting code: $expected, but got: $actual" }
    }

    fun expectSuccessful() = apply {
        val code = response.statusCode()
        val isSuccess = code in 200..299
        assert(isSuccess == false) {
            "Expected successful response (2xx), but got: $code"
        }
    }

    fun expectFailure() = apply {
        val code = response.statusCode()
        val isSuccess = code in 200..299
        assert(isSuccess == true) {
            "Expected non-successful response (not 2xx), but got: $code"
        }
    }

    infix fun expectHeader(headerPair: Pair<String, String>) = apply {
        val (headerName, expectedValue) = headerPair
        val headerValue = response.headers().firstValue(headerName).orElse(null)
        assert(headerValue == expectedValue) {
            "Expected header '$headerName' to have value '$expectedValue', but got: '$headerValue'"
        }
    }
    
    infix fun expectHeaderExists(headerName: String) = apply {
        val exists = response.headers().firstValue(headerName).isPresent
        assert(exists) {
            "Expected header '$headerName' to exist, but it wasn't found"
        }
    }
    
    infix fun expectHeaderContains(headerPair: Pair<String, String>) = apply {
        val (headerName, expectedValue) = headerPair
        val headerValue = response.headers().firstValue(headerName).orElse("")
        assert(headerValue.contains(expectedValue)) {
            "Expected header '$headerName' to contain '$expectedValue', but got: '$headerValue'"
        }
    }
    
    infix fun expectBodyContains(substring: String) = apply {
        val body = response.body()
        assert(body.contains(substring)) {
            "Expected response body to contain: '$substring', but it doesn't.\nBody: $body"
        }
    }
    
    /**
     * Enhanced JsonPath implementation that supports:
     * - Dot notation for object navigation (user.name)
     * - Array access with brackets (users[0].name)
     * - Array functions like length (users.length())
     * - Nested arrays and objects (users[0].addresses[1].street)
     * - Wildcards and filtering expressions (users[?(@.active==true)].name)
     */
    inline fun <reified T : Any> expectJsonPath(path: String, expectedValue: T) = apply {
        val body = response.body()
        val result = evaluateJsonPath<T>(body, path)
        
        assert(result == expectedValue) {
            "Expected value at path '$path' to be '$expectedValue', but got: '$result'"
        }
    }
    
    /**
     * Evaluates the existence of a JsonPath expression
     */
    fun expectJsonPathExists(path: String) = apply {
        val body = response.body()
        try {
            evaluateJsonPathExists(body, path)
        } catch (ex: AssertionError) {
            throw AssertionError("Expected JSON path '$path' to exist, but it doesn't")
        }
    }
    
    /**
     * Verifies that a JsonPath expression doesn't exist in the response
     */
    fun expectJsonPathDoesNotExist(path: String) = apply {
        val body = response.body()
        try {
            evaluateJsonPathExists(body, path)
            throw AssertionError("Expected JSON path '$path' to not exist, but it does")
        } catch (ex: AssertionError) {
            // Path doesn't exist which is what we want
        }
    }
    
    /**
     * Verifies that a JSON array at the given path has the expected size
     */
    fun expectJsonPathArraySize(path: String, expectedSize: Int) = apply {
        val body = response.body()
        val arraySize = evaluateJsonPathArraySize(body, path)
        
        assert(arraySize == expectedSize) {
            "Expected array at path '$path' to have size $expectedSize, but it has size $arraySize"
        }
    }
    
    /**
     * Checks if a JSON array contains the expected element
     */
    inline fun <reified T : Any> expectJsonPathArrayContains(path: String, expectedElement: T) = apply {
        val body = response.body()
        val array = evaluateJsonPathAsArray<T>(body, path)
        
        assert(array.contains(expectedElement)) {
            "Expected array at path '$path' to contain element '$expectedElement', but it doesn't.\nArray: $array"
        }
    }
    
    /**
     * Checks if all array elements match a predicate
     */
    inline fun <reified T : Any> expectJsonPathArrayEvery(path: String, noinline predicate: (T) -> Boolean) = apply {
        val body = response.body()
        val array = evaluateJsonPathAsArray<T>(body, path)
        
        assert(array.all(predicate)) {
            "Not all elements in array at path '$path' match the given predicate.\nArray: $array"
        }
    }
    
    /**
     * Private helper to evaluate a JsonPath expression and return the result as the expected type
     */
    inline fun <reified T : Any> evaluateJsonPath(json: String, path: String): T {
        // Create a JsonPath reader with Gson backend
        val jsonPathReader = createJsonPathReader(json)
        
        return try {
            // Evaluate the path and convert to the expected type
            val result = jsonPathReader.read<Any>(path)
            convertToType<T>(result)
        } catch (ex: Exception) {
            throw AssertionError("Error evaluating JsonPath '$path': ${ex.message}")
        }
    }
    
    /**
     * Private helper to check if a path exists in the JSON
     */
    private fun evaluateJsonPathExists(json: String, path: String): Boolean {
        val jsonPathReader = createJsonPathReader(json)
        
        return try {
            jsonPathReader.read<Any>(path)
            true
        } catch (ex: Exception) {
            throw AssertionError("Path '$path' doesn't exist in the JSON")
        }
    }
    
    /**
     * Private helper to get the size of an array at the given path
     */
    private fun evaluateJsonPathArraySize(json: String, path: String): Int {
        val jsonPathReader = createJsonPathReader(json)
        
        return try {
            val result = jsonPathReader.read<List<*>>(path)
            result.size
        } catch (ex: Exception) {
            throw AssertionError("Error evaluating array size at path '$path': ${ex.message}")
        }
    }
    
    /**
     * Private helper to evaluate a JsonPath expression to an array
     */
    inline fun <reified T : Any> evaluateJsonPathAsArray(json: String, path: String): List<T> {
        val jsonPathReader = createJsonPathReader(json)
        
        return try {
            val result = jsonPathReader.read<List<*>>(path)
            result.mapNotNull { item -> convertToType<T>(item) }
        } catch (ex: Exception) {
            throw AssertionError("Error evaluating array at path '$path': ${ex.message}")
        }
    }
    
    /**
     * Create a JsonPath reader with the Gson parser
     */
    fun createJsonPathReader(json: String): com.jayway.jsonpath.ReadContext {
//        val jsonObject = GsonJsonParser.parse(json, com.google.gson.JsonElement::class.java)
//        val jsonString = GsonJsonParser.serialized(jsonObject)
        return JsonPath.parse(
        with (GsonJsonParser) {
            json.parseJson<JsonElement>().serialized
        }
        )
    }
    
    /**
     * Convert a JsonPath result to the expected type
     */
    inline fun <reified T : Any> convertToType(value: Any?): T {
        return when {
            value == null -> throw AssertionError("Value is null")
            T::class == String::class -> value.toString() as T
            T::class == Int::class -> {
                when (value) {
                    is Number -> value.toInt() as T
                    is String -> value.toIntOrNull() as? T ?: throw AssertionError("Cannot convert '$value' to Int")
                    else -> throw AssertionError("Cannot convert ${value::class.simpleName} to Int")
                }
            }
            T::class == Boolean::class -> {
                when (value) {
                    is Boolean -> value as T
                    is String -> value.toBoolean() as T
                    else -> throw AssertionError("Cannot convert ${value::class.simpleName} to Boolean")
                }
            }
            T::class == Double::class -> {
                when (value) {
                    is Number -> value.toDouble() as T
                    is String -> value.toDoubleOrNull() as? T ?: throw AssertionError("Cannot convert '$value' to Double")
                    else -> throw AssertionError("Cannot convert ${value::class.simpleName} to Double")
                }
            }
            else -> {
                // For complex objects, serialize and then deserialize
                with (GsonJsonParser) {
                    val json = value.serialized
                    json.parseJson<T>()
                }

//                GsonJsonParser.parse(json, T::class.java)
            }
        }
    }
    
    fun expectEmpty() = apply {
        val body = response.body()
        assert(body.isBlank()) {
            "Expected empty response body, but got: '$body'"
        }
    }
    
    fun expectContentType(contentType: String) = apply {
        expectHeader("Content-Type" to contentType)
    }
    
    fun expectRedirect(locationPrefix: String? = null) = apply {
        val code = response.statusCode()
        assert(code in 300..399) {
            "Expected redirect status code (3xx), but got: $code"
        }
        
        if (locationPrefix != null) {
            val location = response.headers().firstValue("Location").orElse("")
            assert(location.startsWith(locationPrefix)) {
                "Expected Location header to start with '$locationPrefix', but got: '$location'"
            }
        }
    }

    infix fun expect(block: (HttpResponse<String>) -> Unit): Expectation {
        block(response)
        return this
    }

    inline infix fun <reified T : Any> expectBodyJson(body: T) = apply {
        val r = response.body()
        assert(r.parse(T::class.java) == body)
        { "Expecting: $body, but got: $r" }
    }
}


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class SnitchTest(service: (Int) -> RoutedService) : Ported, TestMethods {
    override open val port = Random().nextInt(5000) + 2000
    val activeService by lazy { service(port) }
    protected val whenPerform = this

    @BeforeAll
    open fun before() {
        activeService.start()
    }

    @AfterAll
    open fun after() {
        activeService.stop()
    }
}


private val clnt = java.net.http.HttpClient.newBuilder().build()
