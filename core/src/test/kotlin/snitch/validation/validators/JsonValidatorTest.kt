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
import snitch.validation.ofJson

class JsonValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        assertEquals("valid JSON string", ofJson.description)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "{\"key\":\"value\"}",
        "[1,2,3]",
        "{\"nested\":{\"data\":true}}",
        "[{\"item\":1},{\"item\":2}]",
        "\"simple string\"",
        "{}",
        "[]",
        "{\"key\":null}",
        "{\"key\":123}",
        "{\"key\":true}"
    ])
    fun `regex matches valid JSON strings`(input: String) {
        assertTrue(ofJson.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "",
        "invalid",
        "not json",
        "{ unclosed",
        "unclosed\"",
        "[unclosed",
        "}invalid{",
        "]invalid[",
        "123", // Not enclosed in quotes, array, or object
        "true", // Not enclosed in quotes, array, or object
        "null" // Not enclosed in quotes, array, or object
    ])
    fun `regex rejects invalid JSON strings`(input: String) {
        assertFalse(ofJson.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "{\"key\":\"value\"}",
        "[1,2,3]",
        "\"simple string\"",
        "{}",
        "[]"
    ])
    fun `parse returns the JSON string as-is`(input: String) {
        assertEquals(input, ofJson.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse throws for invalid JSON format (object)`() {
        assertThrows<IllegalArgumentException> {
            ofJson.parse(mockParser, listOf("invalid{json}"))
        }
    }
    
    @Test
    fun `parse throws for invalid JSON format (array)`() {
        assertThrows<IllegalArgumentException> {
            ofJson.parse(mockParser, listOf("invalid[json]"))
        }
    }

    @Test
    fun `parse throws for invalid JSON format (string)`() {
        assertThrows<IllegalArgumentException> {
            ofJson.parse(mockParser, listOf("\"unterminated"))
        }
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            ofJson.parse(mockParser, listOf("{}", "[]"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            ofJson.parse(mockParser, emptyList())
        }
    }
}