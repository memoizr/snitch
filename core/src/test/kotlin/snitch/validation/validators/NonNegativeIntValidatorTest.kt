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
import snitch.validation.ofNonNegativeInt

class NonNegativeIntValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        assertEquals("non negative integer", ofNonNegativeInt.description)
    }

    @ParameterizedTest
    @ValueSource(strings = ["0", "1", "42", "999", "2147483647"])
    fun `regex matches valid non-negative integers`(input: String) {
        assertTrue(ofNonNegativeInt.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "-1", "-42", "3.14", "text", "1a", "a1", " ", " 1", "1 "])
    fun `regex rejects invalid non-negative integers`(input: String) {
        assertFalse(ofNonNegativeInt.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = ["0", "1", "42", "999", "2147483647"])
    fun `parse returns correct integer for valid strings`(input: String) {
        val expectedInt = input.toInt()
        assertEquals(expectedInt, ofNonNegativeInt.parse(mockParser, listOf(input)))
    }

    @Test
    fun `parse zero`() {
        assertEquals(0, ofNonNegativeInt.parse(mockParser, listOf("0")))
    }

    @Test
    fun `parse positive integer`() {
        assertEquals(42, ofNonNegativeInt.parse(mockParser, listOf("42")))
    }

    @Test
    fun `parse INTEGER_MAX_VALUE`() {
        assertEquals(Int.MAX_VALUE, ofNonNegativeInt.parse(mockParser, listOf(Int.MAX_VALUE.toString())))
    }

    @Test
    fun `parse throws for integer overflow`() {
        val overflowValue = (Int.MAX_VALUE.toLong() + 1L).toString()
        assertThrows<NumberFormatException> {
            ofNonNegativeInt.parse(mockParser, listOf(overflowValue))
        }
    }
    
    @Test
    fun `parse throws for negative values`() {
        // Negative values shouldn't match the regex, but if they did somehow...
        assertThrows<IllegalArgumentException> {
            ofNonNegativeInt.parse(mockParser, listOf("-1"))
        }
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            ofNonNegativeInt.parse(mockParser, listOf("1", "2"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            ofNonNegativeInt.parse(mockParser, emptyList())
        }
    }
}