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
import snitch.validation.ofIntRange

class IntRangeValidatorTest {

    private val mockParser = mockk<Parser>()
    private val min = 1
    private val max = 100
    private val rangeValidator = ofIntRange(min, max)

    @Test
    fun `validator has correct description`() {
        assertEquals("integer between $min and $max", rangeValidator.description)
    }

    @ParameterizedTest
    @ValueSource(strings = ["1", "50", "100", "-1", "-100"])
    fun `regex matches valid integers`(input: String) {
        assertTrue(rangeValidator.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "1.5", "text", "1a", "a1", " ", " 1", "1 "])
    fun `regex rejects invalid integers`(input: String) {
        assertFalse(rangeValidator.regex.matches(input))
    }

    @Test
    fun `parse returns correct integer for minimum value`() {
        assertEquals(min, rangeValidator.parse(mockParser, listOf(min.toString())))
    }

    @Test
    fun `parse returns correct integer for maximum value`() {
        assertEquals(max, rangeValidator.parse(mockParser, listOf(max.toString())))
    }

    @Test
    fun `parse returns correct integer for value within range`() {
        val value = 50
        assertEquals(value, rangeValidator.parse(mockParser, listOf(value.toString())))
    }

    @Test
    fun `parse throws for value below minimum`() {
        val belowMin = min - 1
        assertThrows<IllegalArgumentException> {
            rangeValidator.parse(mockParser, listOf(belowMin.toString()))
        }
    }

    @Test
    fun `parse throws for value above maximum`() {
        val aboveMax = max + 1
        assertThrows<IllegalArgumentException> {
            rangeValidator.parse(mockParser, listOf(aboveMax.toString()))
        }
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            rangeValidator.parse(mockParser, listOf("10", "20"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            rangeValidator.parse(mockParser, emptyList())
        }
    }

    @Test
    fun `validator with negative range works correctly`() {
        val negativeRangeValidator = ofIntRange(-100, -1)
        
        // Valid within range
        assertEquals(-50, negativeRangeValidator.parse(mockParser, listOf("-50")))
        assertEquals(-100, negativeRangeValidator.parse(mockParser, listOf("-100")))
        assertEquals(-1, negativeRangeValidator.parse(mockParser, listOf("-1")))
        
        // Invalid outside range
        assertThrows<IllegalArgumentException> {
            negativeRangeValidator.parse(mockParser, listOf("0"))
        }
        
        assertThrows<IllegalArgumentException> {
            negativeRangeValidator.parse(mockParser, listOf("-101"))
        }
    }

    @Test
    fun `validator with zero-crossing range works correctly`() {
        val zeroCrossingValidator = ofIntRange(-10, 10)
        
        // Valid within range
        assertEquals(-10, zeroCrossingValidator.parse(mockParser, listOf("-10")))
        assertEquals(0, zeroCrossingValidator.parse(mockParser, listOf("0")))
        assertEquals(10, zeroCrossingValidator.parse(mockParser, listOf("10")))
        
        // Invalid outside range
        assertThrows<IllegalArgumentException> {
            zeroCrossingValidator.parse(mockParser, listOf("-11"))
        }
        
        assertThrows<IllegalArgumentException> {
            zeroCrossingValidator.parse(mockParser, listOf("11"))
        }
    }
}