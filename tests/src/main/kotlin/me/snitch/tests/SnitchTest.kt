package me.snitch.tests

import me.snitch.service.RoutedService
import me.snitch.parsers.GsonJsonParser
import me.snitch.parsers.GsonJsonParser.parse
import java.net.URI
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.*
import me.snitch.tests.TestMethods.HttpClient.delete
import me.snitch.tests.TestMethods.HttpClient.get
import me.snitch.tests.TestMethods.HttpClient.patch
import me.snitch.tests.TestMethods.HttpClient.post
import me.snitch.tests.TestMethods.HttpClient.put
import java.net.BindException
import java.net.ConnectException

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
        fun post(url: String, headers: Map<String, String>, body: String?) =
            call(url, headers) { POST(HttpRequest.BodyPublishers.ofString(body.orEmpty())) }

        fun put(url: String, headers: Map<String, String>, body: String?) =
            call(url, headers) { PUT(HttpRequest.BodyPublishers.ofString(body.orEmpty())) }

        fun delete(url: String, headers: Map<String, String>) = call(url, headers) { DELETE() }
        fun patch(url: String, headers: Map<String, String>, body: String?) =
            call(url, headers) { method("PATCH", HttpRequest.BodyPublishers.ofString(body.orEmpty())) }
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
                    if (body is String) body else body?.serialized
                )

                HttpMethod.POST -> post(
                    "http://localhost:${port}$endpoint",
                    headers,
                    if (body is String) body else body?.serialized
                )

                HttpMethod.DELETE -> delete("http://localhost:${port}$endpoint", headers)
                HttpMethod.PATCH -> patch(
                    "http://localhost:${port}$endpoint", headers,
                    if (body is String) body else body?.serialized
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
        com.memoizr.assertk.expect that response.body() isEqualTo body
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
        com.memoizr.assertk.expect that r.parse(T::class.java) isEqualTo body
    }
}


abstract class SnitchTest(service: (Int) -> RoutedService) : Ported, TestMethods {
    override open val port = Random().nextInt(5000) + 2000
    val activeService by lazy { service(port) }
    protected val whenPerform = this

    open fun before() {
        activeService.start()
    }

    open fun after() {
        activeService.stop()
    }
}

private val clnt = java.net.http.HttpClient.newBuilder().build()
