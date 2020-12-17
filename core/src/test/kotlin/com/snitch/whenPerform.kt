package com.snitch

import com.snitch.extensions.json
import com.snitch.extensions.print
import com.snitch.extensions.toHashMap
import com.memoizr.assertk.expect
import khttp.responses.Response
import org.json.JSONException
import org.json.JSONObject
import java.util.*

abstract class SparkTest {
    protected val whenPerform = this

    protected val port = Random().nextInt(5000) + 2000

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

    enum class HttpMethod {
        POST, GET, PUT, DELETE;
    }

    data class Expectation(
            val port: Int,
            private val method: HttpMethod,
            private val endpoint: String,
            private val headers: Map<String, String> = emptyMap(),
            private val body: Any? = null
    ) {

        private val response by lazy {
            when (method) {
                HttpMethod.GET -> khttp.get("http://localhost:${port}$endpoint", headers = headers, json = body?.toHashMap())
                HttpMethod.POST -> khttp.post("http://localhost:${port}$endpoint", headers = headers, json = body?.toHashMap())
                HttpMethod.PUT -> khttp.put("http://localhost:${port}$endpoint", headers = headers, json = body?.toHashMap())
                HttpMethod.DELETE -> khttp.delete("http://localhost:${port}$endpoint", headers = headers, json = body?.toHashMap())
            }
        }

        infix fun withBody(body: Any) = copy(body = body)

        infix fun withHeaders(headers: Map<String, Any?>) = copy(headers = headers.map { it.key to it.value.toString() }.toMap())

        infix fun expectBody(body: String) = apply {
            expect that response.text isEqualTo body
        }

        infix fun expectCode(code: Int) = apply {
            expect that response.statusCode isEqualTo code
        }

        infix fun expect(block: (Response) -> Unit): Expectation {
            block(response)
            return this
        }

        infix fun expectBodyJson(body: Any) = apply {
            try {
                expect that response.jsonObject.toString() isEqualTo JSONObject(body.json).toString()
            } catch (e : JSONException) {
                body.print()
                throw e
            }
        }
    }
}

