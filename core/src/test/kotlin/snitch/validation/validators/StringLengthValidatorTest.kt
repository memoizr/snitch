package snitch.validation.validators

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import snitch.types.Parser
import snitch.validation.ofStringLength

class StringLengthValidatorTest {

    private val mockParser = mockk<Parser>()
    private val minLength = 3
    private val maxLength = 10
    private val stringLengthValidator = ofStringLength(minLength, maxLength)

    @Test
    fun `validator has correct description`() {
        assertEquals("string with length between $minLength and $maxLength characters", stringLengthValidator.description)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "abc",           // Minimum length
        "abcdefghij",    // Maximum length
        "abcde",         // Middle length
        "123456",        // Numeric
        "abc123",        // Alphanumeric
        "a b c d",       // With spaces
        "a\nb\nc",       // With newlines
        "!@#$%^",        // Special characters
        "αβγδε"          // Unicode characters
    ])
    fun `regex matches valid string lengths`(input: String) {
        assertTrue(stringLengthValidator.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "",              // Empty string
        "a",             // Too short (1 char)
        "ab",            // Too short (2 chars)
        "abcdefghijk",   // Too long (11 chars)
        "abcdefghijklmnop" // Way too long
    ])
    fun `regex rejects invalid string lengths`(input: String) {
        assertFalse(stringLengthValidator.regex.matches(input))
    }

    @Test
    fun `parse returns string at minimum length`() {
        val input = "abc" // 3 chars
        assertEquals(input, stringLengthValidator.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse returns string at maximum length`() {
        val input = "abcdefghij" // 10 chars
        assertEquals(input, stringLengthValidator.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse returns string with middle length`() {
        val input = "abcdef" // 6 chars
        assertEquals(input, stringLengthValidator.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse throws for string below minimum length`() {
        // This shouldn't match the regex, but testing the logic too
        assertThrows<IllegalArgumentException> {
            stringLengthValidator.parse(mockParser, listOf("ab")) // 2 chars
        }
    }

    @Test
    fun `parse throws for string above maximum length`() {
        // This shouldn't match the regex, but testing the logic too
        assertThrows<IllegalArgumentException> {
            stringLengthValidator.parse(mockParser, listOf("abcdefghijk")) // 11 chars
        }
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            stringLengthValidator.parse(mockParser, listOf("abc", "def"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            stringLengthValidator.parse(mockParser, emptyList())
        }
    }

    @Test
    fun `validator with same min and max length works correctly`() {
        val exactLengthValidator = ofStringLength(5, 5)
        
        // Valid exact length
        assertEquals("12345", exactLengthValidator.parse(mockParser, listOf("12345")))
        
        // Invalid - too short
        assertThrows<IllegalArgumentException> {
            exactLengthValidator.parse(mockParser, listOf("1234"))
        }
        
        // Invalid - too long
        assertThrows<IllegalArgumentException> {
            exactLengthValidator.parse(mockParser, listOf("123456"))
        }
    }
    
    @Test
    @Disabled
    fun `validator handles multibyte characters correctly`() {
        // Emoji and special characters that can be multi-byte
        val input = "👨‍👩‍👧‍👦" // Family emoji (may appear as a single character visually)
        val emojiValidator = ofStringLength(1, 10)
        
        // The actual string length will depend on how Kotlin/JVM counts the characters
        // This test verifies that our validator handles these special cases consistently
        val result = emojiValidator.parse(mockParser, listOf(input))
        assertEquals(input, result)
    }
}