package snitch.validation.validators

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import snitch.types.Parser
import snitch.validation.ofRegexPattern

class RegexPatternValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        val description = "US zip code"
        val validator = ofRegexPattern("""^\d{5}(-\d{4})?$""", description)
        assertEquals(description, validator.description)
    }

    @Test
    fun `regex matches valid pattern for zip codes`() {
        val pattern = """^\d{5}(-\d{4})?$"""
        val validator = ofRegexPattern(pattern, "US zip code")
        
        // Basic 5-digit zip
        assertTrue(validator.regex.matches("12345"))
        
        // 9-digit zip with dash
        assertTrue(validator.regex.matches("12345-6789"))
    }

    @Test
    fun `regex rejects invalid pattern for zip codes`() {
        val pattern = """^\d{5}(-\d{4})?$"""
        val validator = ofRegexPattern(pattern, "US zip code")
        
        // Empty string
        assertFalse(validator.regex.matches(""))
        
        // Fewer than 5 digits
        assertFalse(validator.regex.matches("1234"))
        
        // More than 5 digits without hyphen
        assertFalse(validator.regex.matches("123456"))
        
        // Incorrect format for ZIP+4
        assertFalse(validator.regex.matches("12345-678"))
        assertFalse(validator.regex.matches("12345-67890"))
        assertFalse(validator.regex.matches("12345_6789"))
        
        // Non-numeric characters
        assertFalse(validator.regex.matches("abcde"))
        assertFalse(validator.regex.matches("123ab"))
    }

    @Test
    fun `regex matches valid pattern for hex color`() {
        val pattern = """^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$"""
        val validator = ofRegexPattern(pattern, "hex color code")
        
        // 3-digit hex
        assertTrue(validator.regex.matches("#123"))
        assertTrue(validator.regex.matches("#abc"))
        assertTrue(validator.regex.matches("#ABC"))
        
        // 6-digit hex
        assertTrue(validator.regex.matches("#123456"))
        assertTrue(validator.regex.matches("#abcdef"))
        assertTrue(validator.regex.matches("#ABCDEF"))
        assertTrue(validator.regex.matches("#a1B2c3"))
    }

    @Test
    fun `regex rejects invalid pattern for hex color`() {
        val pattern = """^#([0-9A-Fa-f]{3}|[0-9A-Fa-f]{6})$"""
        val validator = ofRegexPattern(pattern, "hex color code")
        
        // Missing # prefix
        assertFalse(validator.regex.matches("123"))
        assertFalse(validator.regex.matches("abcdef"))
        
        // Wrong length
        assertFalse(validator.regex.matches("#12"))
        assertFalse(validator.regex.matches("#1234"))
        assertFalse(validator.regex.matches("#12345"))
        assertFalse(validator.regex.matches("#1234567"))
        
        // Invalid characters
        assertFalse(validator.regex.matches("#12G"))
        assertFalse(validator.regex.matches("#GHIJKL"))
    }

    @Test
    fun `parse returns the string that matches pattern`() {
        val pattern = """^\d{5}(-\d{4})?$"""
        val validator = ofRegexPattern(pattern, "US zip code")
        
        val zip5 = "12345"
        assertEquals(zip5, validator.parse(mockParser, listOf(zip5)))
        
        val zip9 = "12345-6789"
        assertEquals(zip9, validator.parse(mockParser, listOf(zip9)))
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        val pattern = """^\d{5}(-\d{4})?$"""
        val validator = ofRegexPattern(pattern, "US zip code")
        
        assertThrows<IllegalArgumentException> {
            validator.parse(mockParser, listOf("12345", "67890"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        val pattern = """^\d{5}(-\d{4})?$"""
        val validator = ofRegexPattern(pattern, "US zip code")
        
        assertThrows<NoSuchElementException> {
            validator.parse(mockParser, emptyList())
        }
    }
}