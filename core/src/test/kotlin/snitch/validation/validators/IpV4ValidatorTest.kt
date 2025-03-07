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
import snitch.validation.ofIpv4

class IpV4ValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        assertEquals("IPv4 address", ofIpv4.description)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "0.0.0.0",          // Minimum value
        "255.255.255.255",  // Maximum value
        "192.168.1.1",      // Common local IP
        "10.0.0.1",         // Private range
        "172.16.0.1",       // Private range
        "127.0.0.1",        // Localhost
        "8.8.8.8",          // Google DNS
        "1.2.3.4",          // Simple IP
        "123.123.123.123"   // Repeated octets
    ])
    fun `regex matches valid IPv4 addresses`(input: String) {
        assertTrue(ofIpv4.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "",                  // Empty string
        "192.168.1",         // Missing octet
        "192.168.1.1.1",     // Extra octet
        "192.168.1.256",     // Octet > 255
        "192.168.1.-1",      // Negative octet
        "192.168.01",        // Missing octet
        "a.b.c.d",           // Non-numeric
        "192,168,1,1",       // Wrong separator
        "192.168.1.",        // Trailing dot
        ".192.168.1.1",      // Leading dot
        "192..168.1.1",      // Double dot
        "192.168.1.1/24",    // With CIDR
        "192.168.1.1:8080",  // With port
        " 192.168.1.1",      // With leading space
        "192.168.1.1 "       // With trailing space
    ])
    fun `regex rejects invalid IPv4 addresses`(input: String) {
        assertFalse(ofIpv4.regex.matches(input))
    }

    @Test
    fun `parse returns IP address string`() {
        val ip = "192.168.1.1"
        assertEquals(ip, ofIpv4.parse(mockParser, listOf(ip)))
    }

    @Test
    fun `parse returns minimum IP address`() {
        val ip = "0.0.0.0"
        assertEquals(ip, ofIpv4.parse(mockParser, listOf(ip)))
    }

    @Test
    fun `parse returns maximum IP address`() {
        val ip = "255.255.255.255"
        assertEquals(ip, ofIpv4.parse(mockParser, listOf(ip)))
    }

    @Test
    fun `parse returns IP with leading zeros intact`() {
        val ip = "001.002.003.004"
        assertEquals(ip, ofIpv4.parse(mockParser, listOf(ip)))
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            ofIpv4.parse(mockParser, listOf("192.168.1.1", "10.0.0.1"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            ofIpv4.parse(mockParser, emptyList())
        }
    }
    
    @Test
    fun `parse handles special addresses correctly`() {
        // Loopback address
        assertEquals("127.0.0.1", ofIpv4.parse(mockParser, listOf("127.0.0.1")))
        
        // Broadcast address
        assertEquals("255.255.255.255", ofIpv4.parse(mockParser, listOf("255.255.255.255")))
        
        // Default route
        assertEquals("0.0.0.0", ofIpv4.parse(mockParser, listOf("0.0.0.0")))
    }
}