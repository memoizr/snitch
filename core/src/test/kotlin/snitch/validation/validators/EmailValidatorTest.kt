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
import snitch.validation.ofEmail

class EmailValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        assertEquals("email address", ofEmail.description)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "test@example.com",
        "test.email@example.com",
        "test+tag@example.com",
        "test@subdomain.example.com",
        "test@example.co.uk",
        "123@example.com",
        "test_email@example.com",
        "test-email@example.com",
        "test.email-with_special.chars@example.com"
    ])
    fun `regex matches valid email addresses`(input: String) {
        assertTrue(ofEmail.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "",
        "test",
        "test@",
        "@example.com",
        "test@example",
        "test@.com",
        "test@example.",
        "test@exam ple.com",
        "test@ example.com",
        "test@example.com.",
        ".test@example.com",
        "test.@example.com",
        "test@example..com"
    ])
    fun `regex rejects invalid email addresses`(input: String) {
        assertFalse(ofEmail.regex.matches(input))
    }

    @Test
    fun `parse returns email string for valid email`() {
        val email = "test@example.com"
        assertEquals(email, ofEmail.parse(mockParser, listOf(email)))
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            ofEmail.parse(mockParser, listOf("test1@example.com", "test2@example.com"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            ofEmail.parse(mockParser, emptyList())
        }
    }
}