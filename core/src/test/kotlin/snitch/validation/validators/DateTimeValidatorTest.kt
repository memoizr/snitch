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
import snitch.validation.ofDateTime
import java.time.LocalDateTime

class DateTimeValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        assertEquals("datetime in ISO-8601 format (YYYY-MM-DDThh:mm:ss)", ofDateTime.description)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "2023-01-01T00:00:00",
        "2020-02-29T12:30:45", // Leap year
        "1900-01-01T00:00:00",
        "2099-12-31T23:59:59"
    ])
    fun `regex matches valid ISO datetimes`(input: String) {
        assertTrue(ofDateTime.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "",
        "2023-01-01", // Missing time component
        "2023-01-01 00:00:00", // Space instead of T
        "2023/01/01T00:00:00", // Wrong date separator
        "2023-01-01T0:0:0", // Missing leading zeros in time
        "2023-01-01T00:00", // Missing seconds
        "2023-01-01T00:00:00Z", // With timezone
        "2023-01-01T00:00:00+01:00", // With timezone offset
    ])
    fun `regex rejects invalid ISO datetimes`(input: String) {
        assertFalse(ofDateTime.regex.matches(input))
    }

    @Test
    fun `parse returns LocalDateTime for valid ISO datetime string`() {
        val dateTimeStr = "2023-01-01T12:34:56"
        val expectedDateTime = LocalDateTime.of(2023, 1, 1, 12, 34, 56)
        assertEquals(expectedDateTime, ofDateTime.parse(mockParser, listOf(dateTimeStr)))
    }

    @Test
    fun `parse returns LocalDateTime for leap year datetime`() {
        val dateTimeStr = "2020-02-29T15:30:45"
        val expectedDateTime = LocalDateTime.of(2020, 2, 29, 15, 30, 45)
        assertEquals(expectedDateTime, ofDateTime.parse(mockParser, listOf(dateTimeStr)))
    }

    @Test
    fun `parse throws for invalid datetime`() {
        // This passes regex but is not a valid date (February 30th)
        assertThrows<IllegalArgumentException> {
            ofDateTime.parse(mockParser, listOf("2023-02-30T12:00:00"))
        }
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            ofDateTime.parse(mockParser, listOf(
                "2023-01-01T00:00:00",
                "2023-01-02T00:00:00"
            ))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<IllegalArgumentException> {
            ofDateTime.parse(mockParser, emptyList())
        }
    }
    
    @Test
    fun `parse returns correct LocalDateTime for start of day`() {
        val dateTimeStr = "2023-01-01T00:00:00"
        val expectedDateTime = LocalDateTime.of(2023, 1, 1, 0, 0, 0)
        assertEquals(expectedDateTime, ofDateTime.parse(mockParser, listOf(dateTimeStr)))
    }
    
    @Test
    fun `parse returns correct LocalDateTime for end of day`() {
        val dateTimeStr = "2023-01-01T23:59:59"
        val expectedDateTime = LocalDateTime.of(2023, 1, 1, 23, 59, 59)
        assertEquals(expectedDateTime, ofDateTime.parse(mockParser, listOf(dateTimeStr)))
    }
    
    @Test
    fun `parse handles date at century boundary`() {
        // Test 2000-01-01 (year 2000)
        val dateTimeStr = "2000-01-01T00:00:00"
        val expectedDateTime = LocalDateTime.of(2000, 1, 1, 0, 0, 0)
        assertEquals(expectedDateTime, ofDateTime.parse(mockParser, listOf(dateTimeStr)))
    }
}