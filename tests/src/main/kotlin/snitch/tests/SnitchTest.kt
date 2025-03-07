package snitch.tests

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
    
    inline fun <reified T : Any> expectJsonPath(path: String, expectedValue: T) = apply {
        val jsonObject = response.body().parse(com.google.gson.JsonObject::class.java)
        // Simple path implementation - split by dots and navigate the JSON tree
        val pathParts = path.split(".")
        var current: com.google.gson.JsonElement = jsonObject
        
        for (part in pathParts) {
            if (current.isJsonObject) {
                current = current.asJsonObject.get(part) 
                    ?: throw AssertionError("JSON path '$path' not found at part '$part'")
            } else {
                throw AssertionError("Cannot navigate further in path '$path' at '$part'")
            }
        }
        
        val actualValue = with(GsonJsonParser) {
            when (T::class) {
                String::class -> current.asString
                Int::class -> current.asInt
                Boolean::class -> current.asBoolean
                Double::class -> current.asDouble
                else -> current.toString().parse(T::class.java)
            }
        }
        
        assert(actualValue == expectedValue) {
            "Expected value at path '$path' to be '$expectedValue', but got: '$actualValue'"
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
