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
import snitch.validation.ofBoolean

class BooleanValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        assertEquals("boolean (true/false, yes/no, 1/0)", ofBoolean.description)
    }

    @ParameterizedTest
    @ValueSource(strings = ["true", "false", "yes", "no", "1", "0", "TRUE", "FALSE", "Yes", "No"])
    fun `regex matches valid boolean values`(input: String) {
        assertTrue(ofBoolean.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "t", "f", "y", "n", "2", "-1", "True ", " False", "truthy", "falsey", "yep", "nope"])
    fun `regex rejects invalid boolean values`(input: String) {
        assertFalse(ofBoolean.regex.matches(input))
    }

    @Test
    fun `parse returns true for 'true'`() {
        assertTrue(ofBoolean.parse(mockParser, listOf("true")))
    }

    @Test
    fun `parse returns true for 'TRUE' case insensitive`() {
        assertTrue(ofBoolean.parse(mockParser, listOf("TRUE")))
    }

    @Test
    fun `parse returns true for 'yes'`() {
        assertTrue(ofBoolean.parse(mockParser, listOf("yes")))
    }

    @Test
    fun `parse returns true for 'YES' case insensitive`() {
        assertTrue(ofBoolean.parse(mockParser, listOf("YES")))
    }

    @Test
    fun `parse returns true for '1'`() {
        assertTrue(ofBoolean.parse(mockParser, listOf("1")))
    }

    @Test
    fun `parse returns false for 'false'`() {
        assertFalse(ofBoolean.parse(mockParser, listOf("false")))
    }

    @Test
    fun `parse returns false for 'FALSE' case insensitive`() {
        assertFalse(ofBoolean.parse(mockParser, listOf("FALSE")))
    }

    @Test
    fun `parse returns false for 'no'`() {
        assertFalse(ofBoolean.parse(mockParser, listOf("no")))
    }

    @Test
    fun `parse returns false for 'NO' case insensitive`() {
        assertFalse(ofBoolean.parse(mockParser, listOf("NO")))
    }

    @Test
    fun `parse returns false for '0'`() {
        assertFalse(ofBoolean.parse(mockParser, listOf("0")))
    }

    @Test
    fun `parse throws for invalid boolean representation`() {
        assertThrows<IllegalArgumentException> {
            ofBoolean.parse(mockParser, listOf("invalid"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            ofBoolean.parse(mockParser, emptyList())
        }
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            ofBoolean.parse(mockParser, listOf("true", "false"))
        }
    }
}