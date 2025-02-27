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
import snitch.validation.ofPhoneNumber

class PhoneNumberValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        assertEquals("phone number (digits, spaces, and +)", ofPhoneNumber.description)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "1234567890",        // US 10-digit
        "+1 234 567 890",    // International with spaces
        "+12345678901",      // International without spaces
        "123 456 7890",      // US with spaces
        "+44 20 1234 5678",  // UK format with spaces
        "+123456789012345",  // Long international number
        "123"                // Very short number
    ])
    fun `regex matches valid phone numbers`(input: String) {
        assertTrue(ofPhoneNumber.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "",                   // Empty string
        "abc",                // Non-numeric
        "123abc456",          // Mixed characters
        "+",                  // Only plus
        "++123456789",        // Multiple plus signs
        "+123 456+789",       // Plus in the middle
        "123.456.7890"        // Periods (not allowed)
    ])
    fun `regex rejects invalid phone numbers`(input: String) {
        assertFalse(ofPhoneNumber.regex.matches(input))
    }

    @Test
    fun `parse returns phone number with spaces removed`() {
        val input = "+1 234 567 890"
        val expected = "+1234567890"
        assertEquals(expected, ofPhoneNumber.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse returns phone number without spaces`() {
        val input = "+12345678901"
        assertEquals(input, ofPhoneNumber.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse removes spaces from US formatted number`() {
        val input = "123 456 7890"
        val expected = "1234567890"
        assertEquals(expected, ofPhoneNumber.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse removes spaces from international number`() {
        val input = "+44 20 1234 5678"
        val expected = "+442012345678"
        assertEquals(expected, ofPhoneNumber.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            ofPhoneNumber.parse(mockParser, listOf("+12345678901", "+09876543210"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            ofPhoneNumber.parse(mockParser, emptyList())
        }
    }
    
    @Test
    fun `parse preserves plus sign at beginning`() {
        val input = "+12345"
        assertEquals("+12345", ofPhoneNumber.parse(mockParser, listOf(input)))
    }
    
    @Test
    fun `parse handles short numbers`() {
        val input = "123"
        assertEquals("123", ofPhoneNumber.parse(mockParser, listOf(input)))
    }
    
    @Test
    fun `parse handles very long numbers`() {
        val input = "+123456789012345678901234"
        assertEquals("+123456789012345678901234", ofPhoneNumber.parse(mockParser, listOf(input)))
    }
}