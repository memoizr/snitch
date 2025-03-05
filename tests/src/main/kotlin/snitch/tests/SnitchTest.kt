package snitch.tests

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import snitch.parsers.GsonJsonParser
import snitch.parsers.GsonJsonParser.parse
import snitch.parsers.GsonJsonParser.serialized
import snitch.service.RoutedService
import snitch.tests.TestMethods.HttpClient.delete
import snitch.tests.TestMethods.HttpClient.get
import snitch.tests.TestMethods.HttpClient.patch
import snitch.tests.TestMethods.HttpClient.post
import snitch.tests.TestMethods.HttpClient.put
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
    infix fun GET(endpoint: String): Expectation {
        return Expectation(port, HttpMethod.GET, endpoint)
    }

    infix fun POST(endpoint: String): Expectation {
        return Expectation(port, HttpMethod.POST, endpoint)
    }

    infix fun DELETE(endpoint: String): Expectation {
        return Expectation(port, HttpMethod.DELETE, endpoint)
    }

    infix fun PUT(endpoint: String): Expectation {
        return Expectation(port, HttpMethod.PUT, endpoint)
    }

    infix fun PATCH(endpoint: String): Expectation {
        return Expectation(port, HttpMethod.PATCH, endpoint)
    }

    object HttpClient {

        fun call(
            url: String,
            headers: Map<String, String>,
            fn: HttpRequest.Builder.() -> HttpRequest.Builder
        ) =
            retry {
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

        fun get(url: String, headers: Map<String, String>) = call(url, headers) { GET() }

        fun post(url: String, headers: Map<String, String>, body: Any?) =
            call(url, headers) { POST(getBodyPublisher(body)) }

        fun put(url: String, headers: Map<String, String>, body: Any?) =
            call(url, headers) { PUT(getBodyPublisher(body)) }

        fun patch(url: String, headers: Map<String, String>, body: Any?) =
            call(url, headers) { method("PATCH", getBodyPublisher(body)) }

        fun delete(url: String, headers: Map<String, String>) = call(url, headers) { DELETE() }

        private fun getBodyPublisher(body: Any?) = when (body) {
            is String? -> HttpRequest.BodyPublishers.ofString(body.orEmpty())
            is ByteArray? -> HttpRequest.BodyPublishers.ofByteArray(body ?: byteArrayOf())
            else -> HttpRequest.BodyPublishers.ofString(body?.serialized.orEmpty())
        }
    }

}

enum class HttpMethod {
    POST, GET, PUT, DELETE, PATCH;
}

data class Expectation(
    val port: Int,
    private val method: HttpMethod,
    private val endpoint: String,
    private val headers: Map<String, String> = emptyMap(),
    private val body: Any? = null
) {
    val response: HttpResponse<String> by lazy {
        with(GsonJsonParser) {
            when (method) {
                HttpMethod.GET -> get("http://localhost:${port}$endpoint", headers)
                HttpMethod.PUT -> put(
                    "http://localhost:${port}$endpoint",
                    headers,
                    body
                )

                HttpMethod.POST -> post(
                    "http://localhost:${port}$endpoint",
                    headers,
                    body
                )

                HttpMethod.DELETE -> delete("http://localhost:${port}$endpoint", headers)
                HttpMethod.PATCH -> patch(
                    "http://localhost:${port}$endpoint", headers,
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

    infix fun expect(block: (HttpResponse<String>) -> Unit): Expectation {
        block(response)
        return this
    }

    infix inline fun <reified T : Any> expectBodyJson(body: T) = apply {
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
