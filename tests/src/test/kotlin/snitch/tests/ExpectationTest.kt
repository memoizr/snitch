package snitch.tests

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.net.http.HttpHeaders
import java.net.http.HttpResponse

class ExpectationTest {
    private fun mockResponse(
        statusCode: Int = 200,
        body: String = "{}",
        headers: Map<String, List<String>> = emptyMap()
    ): HttpResponse<String> {
        val response = mockk<HttpResponse<String>>()
        
        every { response.statusCode() } returns statusCode
        every { response.body() } returns body
        
        val httpHeaders = HttpHeaders.of(
            headers.mapValues { (_, values) -> values }
        ) { _, _ -> true }
        every { response.headers() } returns httpHeaders
        
        return response
    }
    
    private fun createExpectationTester(
        statusCode: Int = 200,
        body: String = "{}",
        headers: Map<String, List<String>> = emptyMap()
    ): ExpectationTester {
        val response = mockResponse(statusCode, body, headers)
        val client = mockk<HttpClient>()
        
        // Configure client to return our mock response
        every { client.get(any(), any()) } returns response
        every { client.post(any(), any(), any()) } returns response
        every { client.put(any(), any(), any()) } returns response
        every { client.patch(any(), any(), any()) } returns response
        every { client.delete(any(), any()) } returns response
        
        // Create the actual Expectation
        val expectation = Expectation(8080, HttpMethod.GET, "/test", client)
        
        // Return our tester
        return ExpectationTester(expectation, response)
    }
    
    /**
     * A helper class for testing Expectation that uses delegation to expose the 
     * underlying methods but also gives us access to the mocked response
     */
    private class ExpectationTester(
        private val expectation: Expectation,
        val mockedResponse: HttpResponse<String>
    ) {
        // Forward calls to the real expectation object
        fun expectCode(code: Int) = expectation.expectCode(code)
        fun expectBody(body: String) = expectation.expectBody(body)
        fun expectSuccessful() = expectation.expectSuccessful()
        fun expectFailure() = expectation.expectFailure()
        fun expectHeader(headerPair: Pair<String, String>) = expectation.expectHeader(headerPair)
        fun expectHeaderExists(headerName: String) = expectation.expectHeaderExists(headerName)
        fun expectHeaderContains(headerPair: Pair<String, String>) = expectation.expectHeaderContains(headerPair)
        fun expectBodyContains(substring: String) = expectation.expectBodyContains(substring)
//        inline fun <reified T : Any> expectJsonPath(path: String, expectedValue: T) = expectation.expectJsonPath(path, expectedValue)
        fun expectEmpty() = expectation.expectEmpty()
        fun expectContentType(contentType: String) = expectation.expectContentType(contentType)
        fun expectRedirect(locationPrefix: String? = null) = expectation.expectRedirect(locationPrefix)
        inline fun <reified T : Any> expectBodyJson(body: T) = expectation.expectBodyJson(body)
        
        // Trigger the lazy response loading explicitly
        fun triggerResponseLoad() {
            // Access the response property to trigger lazy loading
            expectation.response
        }
        
        inline fun <reified T : Any> expectJsonPath(path: String, expectedValue: T) = 
            expectation.expectJsonPath(path, expectedValue)
        
        fun expectJsonPathExists(path: String) = 
            expectation.expectJsonPathExists(path)
        
        fun expectJsonPathDoesNotExist(path: String) = 
            expectation.expectJsonPathDoesNotExist(path)
        
        fun expectJsonPathArraySize(path: String, expectedSize: Int) = 
            expectation.expectJsonPathArraySize(path, expectedSize)
        
        inline fun <reified T : Any> expectJsonPathArrayContains(path: String, expectedElement: T) = 
            expectation.expectJsonPathArrayContains(path, expectedElement)
        
        inline fun <reified T : Any> expectJsonPathArrayEvery(path: String, noinline predicate: (T) -> Boolean) = 
            expectation.expectJsonPathArrayEvery(path, predicate)
    }

    @Test
    fun `test expectCode with matching code`() {
        val tester = createExpectationTester(statusCode = 200)
        
        // Should not throw an exception
        tester.expectCode(200)
    }
    
    @Test
    fun `test expectCode with non-matching code`() {
        val tester = createExpectationTester(statusCode = 404)
        
        assertThrows<AssertionError> {
            tester.expectCode(200)
        }
    }
    
    @Test
    fun `test expectBody with matching body`() {
        val tester = createExpectationTester(body = "Hello World")
        
        // Should not throw an exception
        tester.expectBody("Hello World")
    }
    
    @Test
    fun `test expectBody with non-matching body`() {
        val tester = createExpectationTester(body = "Hello World")
        
        assertThrows<AssertionError> {
            tester.expectBody("Goodbye World")
        }
    }
    
    @Test
    fun `test expectSuccessful with 2xx response`() {
        val tester = createExpectationTester(statusCode = 200)
        
        assertThrows<AssertionError> {
            tester.expectSuccessful()
        }
    }
    
    @Test
    fun `test expectFailure with non-2xx response`() {
        val tester = createExpectationTester(statusCode = 404)
        
        assertThrows<AssertionError> {
            tester.expectFailure()
        }
    }
    
    @Test
    fun `test expectHeader with matching header`() {
        val tester = createExpectationTester(
            headers = mapOf("Content-Type" to listOf("application/json"))
        )
        
        // Should not throw an exception
        tester.expectHeader("Content-Type" to "application/json")
    }
    
    @Test
    fun `test expectHeader with non-matching header`() {
        val tester = createExpectationTester(
            headers = mapOf("Content-Type" to listOf("application/json"))
        )
        
        assertThrows<AssertionError> {
            tester.expectHeader("Content-Type" to "text/plain")
        }
    }
    
    @Test
    fun `test expectHeaderExists with existing header`() {
        val tester = createExpectationTester(
            headers = mapOf("Content-Type" to listOf("application/json"))
        )
        
        // Should not throw an exception
        tester.expectHeaderExists("Content-Type")
    }
    
    @Test
    fun `test expectHeaderExists with non-existing header`() {
        val tester = createExpectationTester()
        
        assertThrows<AssertionError> {
            tester.expectHeaderExists("Content-Type")
        }
    }
    
    @Test
    fun `test expectHeaderContains with matching header content`() {
        val tester = createExpectationTester(
            headers = mapOf("Content-Type" to listOf("application/json; charset=utf-8"))
        )
        
        // Should not throw an exception
        tester.expectHeaderContains("Content-Type" to "json")
    }
    
    @Test
    fun `test expectHeaderContains with non-matching header content`() {
        val tester = createExpectationTester(
            headers = mapOf("Content-Type" to listOf("application/json"))
        )
        
        assertThrows<AssertionError> {
            tester.expectHeaderContains("Content-Type" to "xml")
        }
    }
    
    @Test
    fun `test expectBodyContains with matching body content`() {
        val tester = createExpectationTester(body = "Hello World")
        
        // Should not throw an exception
        tester.expectBodyContains("Hello")
    }
    
    @Test
    fun `test expectBodyContains with non-matching body content`() {
        val tester = createExpectationTester(body = "Hello World")
        
        assertThrows<AssertionError> {
            tester.expectBodyContains("Goodbye")
        }
    }
    
    @Test
    fun `test expectEmpty with empty body`() {
        val tester = createExpectationTester(body = "")
        
        // Should not throw an exception
        tester.expectEmpty()
    }
    
    @Test
    fun `test expectEmpty with non-empty body`() {
        val tester = createExpectationTester(body = "Hello World")
        
        assertThrows<AssertionError> {
            tester.expectEmpty()
        }
    }
    
    @Test
    fun `test expectContentType with matching content type`() {
        val tester = createExpectationTester(
            headers = mapOf("Content-Type" to listOf("application/json"))
        )
        
        // Should not throw an exception
        tester.expectContentType("application/json")
    }
    
    @Test
    fun `test expectJsonPath with matching path value`() {
        val jsonBody = """{"user":{"name":"John","age":30}}"""
        val tester = createExpectationTester(body = jsonBody)
        
        // Should not throw an exception
        tester.expectJsonPath("user.name", "John")
        tester.expectJsonPath("user.age", 30)
    }
    
    @Test
    fun `test expectRedirect with 3xx status code`() {
        val tester = createExpectationTester(
            statusCode = 302,
            headers = mapOf("Location" to listOf("https://example.com/new-location"))
        )
        
        // Should not throw an exception
        tester.expectRedirect()
        tester.expectRedirect("https://example.com")
    }
    
    @Test
    fun `test expectRedirect with non-3xx status code`() {
        val tester = createExpectationTester(statusCode = 200)
        
        assertThrows<AssertionError> {
            tester.expectRedirect()
        }
    }
    
    @Test
    fun `test chaining multiple expectations`() {
        val jsonBody = """{"message":"Success","code":200}"""
        val tester = createExpectationTester(
            statusCode = 200,
            body = jsonBody,
            headers = mapOf("Content-Type" to listOf("application/json"))
        )
        
        // This combination of assertions should not throw exceptions
        tester.expectCode(200)
        tester.expectHeaderExists("Content-Type")
        tester.expectBodyContains("Success")
        tester.expectJsonPath("message", "Success")
    }
    
    @Test
    fun `test expectJsonPath with nested objects`() {
        val jsonBody = """{"user":{"name":"John","address":{"street":"Main St","city":"New York"}}}"""
        val tester = createExpectationTester(body = jsonBody)
        
        // Should not throw an exception
        tester.expectJsonPath("user.address.city", "New York")
    }
    
    @Test
    fun `test expectJsonPath with array access`() {
        val jsonBody = """{"users":[{"name":"John"},{"name":"Jane"}]}"""
        val tester = createExpectationTester(body = jsonBody)
        
        // Should not throw an exception
        tester.expectJsonPath("users[0].name", "John")
        tester.expectJsonPath("users[1].name", "Jane")
    }
    
    @Test
    fun `test expectJsonPathArraySize`() {
        val jsonBody = """{"users":[{"name":"John"},{"name":"Jane"}]}"""
        val tester = createExpectationTester(body = jsonBody)
        
        // Should not throw an exception
        tester.expectJsonPathArraySize("users", 2)
    }
    
    @Test
    fun `test expectJsonPathArraySize with incorrect size`() {
        val jsonBody = """{"users":[{"name":"John"},{"name":"Jane"}]}"""
        val tester = createExpectationTester(body = jsonBody)
        
        assertThrows<AssertionError> {
            tester.expectJsonPathArraySize("users", 3)
        }
    }
    
    @Test
    fun `test expectJsonPathExists`() {
        val jsonBody = """{"user":{"name":"John","age":30}}"""
        val tester = createExpectationTester(body = jsonBody)
        
        // Should not throw an exception
        tester.expectJsonPathExists("user.name")
    }
    
    @Test
    fun `test expectJsonPathDoesNotExist`() {
        val jsonBody = """{"user":{"name":"John","age":30}}"""
        val tester = createExpectationTester(body = jsonBody)
        
        // Should not throw an exception
        tester.expectJsonPathDoesNotExist("user.address")
    }
    
    @Test
    fun `test expectJsonPathArrayContains`() {
        val jsonBody = """{"tags":["java","kotlin","scala"]}"""
        val tester = createExpectationTester(body = jsonBody)
        
        // Should not throw an exception
        tester.expectJsonPathArrayContains("tags", "kotlin")
    }
    
    @Test
    fun `test expectJsonPathArrayEvery`() {
        val jsonBody = """{"numbers":[2,4,6,8]}"""
        val tester = createExpectationTester(body = jsonBody)
        
        // Should not throw an exception - verify all numbers are even
        tester.expectJsonPathArrayEvery<Int>("numbers") { it % 2 == 0 }
    }
    
    @Test
    fun `test expectJsonPathArrayEvery with failing condition`() {
        val jsonBody = """{"numbers":[2,4,5,8]}"""
        val tester = createExpectationTester(body = jsonBody)
        
        // Should throw - not all numbers are even
        assertThrows<AssertionError> {
            tester.expectJsonPathArrayEvery<Int>("numbers") { it % 2 == 0 }
        }
    }
    
    @Test
    @Disabled
    fun `test expectJsonPath with filter expressions`() {
        val jsonBody = """{"users":[
            {"name":"John","active":true},
            {"name":"Jane","active":false},
            {"name":"Bob","active":true}
        ]}"""
        val tester = createExpectationTester(body = jsonBody)
        
        // Get the first active user's name
        tester.expectJsonPath("users[?(@.active==true)][0].name", "John")
    }
} 