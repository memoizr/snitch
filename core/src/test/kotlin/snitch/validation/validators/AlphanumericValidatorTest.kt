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
import snitch.validation.ofAlphanumeric

class AlphanumericValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        assertEquals("alphanumeric string", ofAlphanumeric.description)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "abc",
        "123",
        "abc123",
        "ABC",
        "ABC123",
        "aB1",
        "a",
        "1",
        "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789" // All valid characters
    ])
    fun `regex matches valid alphanumeric strings`(input: String) {
        assertTrue(ofAlphanumeric.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "",             // Empty string
        "abc-123",      // Hyphen
        "abc_123",      // Underscore
        "abc 123",      // Space
        "abc.123",      // Period
        "abc@123",      // Special character
        "abç123",       // Non-ASCII character
        "αβγ123",       // Greek letters
        "abc\n123",     // Newline
        "abc\t123"      // Tab
    ])
    fun `regex rejects invalid alphanumeric strings`(input: String) {
        assertFalse(ofAlphanumeric.regex.matches(input))
    }

    @Test
    fun `parse returns lowercase alphanumeric string`() {
        val input = "abc123"
        assertEquals(input, ofAlphanumeric.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse returns uppercase alphanumeric string`() {
        val input = "ABC123"
        assertEquals(input, ofAlphanumeric.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse returns mixed case alphanumeric string`() {
        val input = "aBc123"
        assertEquals(input, ofAlphanumeric.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse returns numeric-only string`() {
        val input = "123456"
        assertEquals(input, ofAlphanumeric.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse returns alphabetic-only string`() {
        val input = "abcdef"
        assertEquals(input, ofAlphanumeric.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            ofAlphanumeric.parse(mockParser, listOf("abc123", "def456"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            ofAlphanumeric.parse(mockParser, emptyList())
        }
    }
    
    @Test
    fun `parse handles single character`() {
        assertEquals("a", ofAlphanumeric.parse(mockParser, listOf("a")))
        assertEquals("1", ofAlphanumeric.parse(mockParser, listOf("1")))
    }
    
    @Test
    fun `parse handles long alphanumeric string`() {
        val longString = "a".repeat(1000) + "1".repeat(1000)
        assertEquals(longString, ofAlphanumeric.parse(mockParser, listOf(longString)))
    }
}