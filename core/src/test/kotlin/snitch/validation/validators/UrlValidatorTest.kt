package snitch.validation.validators

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import snitch.types.Parser
import snitch.validation.ofUrl
import java.net.URI

class UrlValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        assertEquals("URL", ofUrl.description)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "http://example.com",
        "https://example.com",
        "ftp://example.com",
        "http://localhost",
        "http://127.0.0.1",
        "http://example.com/",
        "http://example.com/path",
        "http://example.com/path/to/resource",
        "http://example.com/path?query=value",
        "http://example.com/path?query=value&another=value",
        "http://example.com:8080",
        "http://user:password@example.com",
        "http://sub.example.com",
        "http://example.com/path#fragment",
        "http://example.co.uk",
        "http://xn--80aswg.xn--p1ai", // Punycode for Cyrillic domain
        "https://example.com/path/with/trailing/slash/"
    ])
    fun `regex matches valid URLs`(input: String) {
        assertTrue(ofUrl.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "",
        "example.com",                  // Missing scheme
        "http:/example.com",            // Missing slash
        "http//example.com",            // Missing colon
        "http:/",                       // Incomplete
        "http://",                      // No domain
        "htp://example.com",            // Invalid scheme
        "mailto:user@example.com",      // Non-web URL
        "http:///example.com",          // Extra slash
        "http://example.com/path with space", // Space in path
        "javascript:alert('XSS')",      // JavaScript URL
        "file:///path/to/file",         // File URL
        "\nhttp://example.com",         // Leading newline
        "http://example.com\n",         // Trailing newline
        " http://example.com",          // Leading space
        "http://example.com "           // Trailing space
    ])
    fun `regex rejects invalid URLs`(input: String) {
        assertFalse(ofUrl.regex.matches(input))
    }

    @Test
    fun `parse returns URI for valid URL string`() {
        val urlStr = "http://example.com"
        val expectedUri = URI.create(urlStr)
        assertEquals(expectedUri, ofUrl.parse(mockParser, listOf(urlStr)))
    }

    @Test
    fun `parse returns URI for URL with path`() {
        val urlStr = "http://example.com/path/to/resource"
        val expectedUri = URI.create(urlStr)
        assertEquals(expectedUri, ofUrl.parse(mockParser, listOf(urlStr)))
    }

    @Test
    fun `parse returns URI for URL with query parameters`() {
        val urlStr = "http://example.com/path?query=value&another=value"
        val expectedUri = URI.create(urlStr)
        assertEquals(expectedUri, ofUrl.parse(mockParser, listOf(urlStr)))
    }

    @Test
    fun `parse returns URI for URL with fragment`() {
        val urlStr = "http://example.com/path#fragment"
        val expectedUri = URI.create(urlStr)
        assertEquals(expectedUri, ofUrl.parse(mockParser, listOf(urlStr)))
    }

    @Test
    fun `parse returns URI for URL with port`() {
        val urlStr = "http://example.com:8080"
        val expectedUri = URI.create(urlStr)
        assertEquals(expectedUri, ofUrl.parse(mockParser, listOf(urlStr)))
    }

    @Test
    fun `parse returns URI for URL with credentials`() {
        val urlStr = "http://user:password@example.com"
        val expectedUri = URI.create(urlStr)
        assertEquals(expectedUri, ofUrl.parse(mockParser, listOf(urlStr)))
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            ofUrl.parse(mockParser, listOf("http://example.com", "http://example.org"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            ofUrl.parse(mockParser, emptyList())
        }
    }
    
    @Test
    fun `parse URI components are accessible`() {
        val urlStr = "http://user:password@example.com:8080/path?query=value#fragment"
        val uri = ofUrl.parse(mockParser, listOf(urlStr))
        
        assertEquals("http", uri.scheme)
        assertEquals("user:password", uri.userInfo)
        assertEquals("example.com", uri.host)
        assertEquals(8080, uri.port)
        assertEquals("/path", uri.path)
        assertEquals("query=value", uri.query)
        assertEquals("fragment", uri.fragment)
    }
    
    @Test
    fun `parse different schemes`() {
        // HTTP
        val httpUri = ofUrl.parse(mockParser, listOf("http://example.com"))
        assertEquals("http", httpUri.scheme)
        
        // HTTPS
        val httpsUri = ofUrl.parse(mockParser, listOf("https://example.com"))
        assertEquals("https", httpsUri.scheme)
        
        // FTP
        val ftpUri = ofUrl.parse(mockParser, listOf("ftp://example.com"))
        assertEquals("ftp", ftpUri.scheme)
    }
}