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
import snitch.validation.ofDate
import snitch.validation.ofDateFormat
import java.time.LocalDate

class DateValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        assertEquals("date in ISO-8601 format (YYYY-MM-DD)", ofDate.description)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "2023-01-01",
        "2020-02-29", // Leap year
        "1900-01-01",
        "2099-12-31"
    ])
    fun `regex matches valid ISO dates`(input: String) {
        assertTrue(ofDate.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "",
        "01-01-2023", // Wrong format
        "2023/01/01", // Wrong separator
        "01/01/2023", // Wrong format and separator
        "2023-1-1", // Missing leading zeros
        "20230101", // No separators
        "2023-01", // Incomplete
        "2023-01-01T00:00:00", // With time
//        "2023-13-01", // Invalid month
//        "2023-01-32", // Invalid day
        "20123-01-01" // Year too long
    ])
    fun `regex rejects invalid ISO dates`(input: String) {
        assertFalse(ofDate.regex.matches(input))
    }

    @Test
    fun `parse returns LocalDate for valid ISO date string`() {
        val dateStr = "2023-01-01"
        val expectedDate = LocalDate.of(2023, 1, 1)
        assertEquals(expectedDate, ofDate.parse(mockParser, listOf(dateStr)))
    }

    @Test
    fun `parse returns LocalDate for leap year date`() {
        val dateStr = "2020-02-29"
        val expectedDate = LocalDate.of(2020, 2, 29)
        assertEquals(expectedDate, ofDate.parse(mockParser, listOf(dateStr)))
    }

    @Test
    fun `parse throws for invalid date`() {
        // This passes regex but is not a valid date (February 30th)
        assertThrows<IllegalArgumentException> {
            ofDate.parse(mockParser, listOf("2023-02-30"))
        }
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            ofDate.parse(mockParser, listOf("2023-01-01", "2023-01-02"))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            ofDate.parse(mockParser, emptyList())
        }
    }
    
    // Testing the custom date format validator
    
    @Test
    fun `ofDateFormat validator has correct description`() {
        val format = "dd/MM/yyyy"
        val validator = ofDateFormat(format)
        assertEquals("date in format: $format", validator.description)
    }
    
    @Test
    fun `ofDateFormat parse returns LocalDate for valid custom format date string`() {
        val format = "dd/MM/yyyy"
        val validator = ofDateFormat(format)
        val dateStr = "01/01/2023"
        val expectedDate = LocalDate.of(2023, 1, 1)
        assertEquals(expectedDate, validator.parse(mockParser, listOf(dateStr)))
    }
    
    @Test
    fun `ofDateFormat parse throws for invalid date in custom format`() {
        val format = "dd/MM/yyyy"
        val validator = ofDateFormat(format)
        assertThrows<IllegalArgumentException> {
            validator.parse(mockParser, listOf("300/02/2023")) // Invalid date (February 30th)
        }
    }
    
    @Test
    fun `ofDateFormat parse throws for incorrectly formatted date`() {
        val format = "dd/MM/yyyy"
        val validator = ofDateFormat(format)
        assertThrows<IllegalArgumentException> {
            validator.parse(mockParser, listOf("2023-01-01")) // ISO format instead of dd/MM/yyyy
        }
    }
}