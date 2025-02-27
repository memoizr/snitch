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
import snitch.validation.ofUuid
import java.util.*

class UuidValidatorTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `validator has correct description`() {
        assertEquals("UUID", ofUuid.description)
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "123e4567-e89b-12d3-a456-426614174000",
        "00000000-0000-0000-0000-000000000000",
        "123E4567-E89B-12D3-A456-426614174000" // Case insensitive
    ])
    fun `regex matches valid UUIDs`(input: String) {
        assertTrue(ofUuid.regex.matches(input))
    }

    @ParameterizedTest
    @ValueSource(strings = [
        "",
        "123e4567e89b12d3a456426614174000", // Without dashes
        "123e4567-e89b-12d3-a456-42661417400", // Too short
        "123e4567-e89b-12d3-a456-4266141740000", // Too long
        "123g4567-e89b-12d3-a456-426614174000", // Invalid character
        "123e4567-e89b-12d3-a456-426614174000-", // Extra dash
        "-123e4567-e89b-12d3-a456-426614174000" // Extra dash
    ])
    fun `regex rejects invalid UUIDs`(input: String) {
        assertFalse(ofUuid.regex.matches(input))
    }

    @Test
    fun `parse returns UUID for valid UUID string`() {
        val uuidStr = "123e4567-e89b-12d3-a456-426614174000"
        val expectedUuid = UUID.fromString(uuidStr)
        assertEquals(expectedUuid, ofUuid.parse(mockParser, listOf(uuidStr)))
    }

    @Test
    fun `parse returns UUID for uppercase UUID string`() {
        val lowerUuidStr = "123e4567-e89b-12d3-a456-426614174000"
        val upperUuidStr = "123E4567-E89B-12D3-A456-426614174000"
        val expectedUuid = UUID.fromString(lowerUuidStr)
        assertEquals(expectedUuid, ofUuid.parse(mockParser, listOf(upperUuidStr)))
    }

    @Test
    fun `parse returns UUID for zero UUID string`() {
        val zeroUuid = "00000000-0000-0000-0000-000000000000"
        val expectedUuid = UUID.fromString(zeroUuid)
        assertEquals(expectedUuid, ofUuid.parse(mockParser, listOf(zeroUuid)))
    }

    @Test
    fun `parse throws for invalid UUID format`() {
        assertThrows<IllegalArgumentException> {
            ofUuid.parse(mockParser, listOf("invalid-uuid"))
        }
    }

    @Test
    fun `parse throws for collection with multiple items`() {
        assertThrows<IllegalArgumentException> {
            ofUuid.parse(mockParser, listOf(
                "123e4567-e89b-12d3-a456-426614174000",
                "123e4567-e89b-12d3-a456-426614174001"
            ))
        }
    }

    @Test
    fun `parse throws for empty collection`() {
        assertThrows<NoSuchElementException> {
            ofUuid.parse(mockParser, emptyList())
        }
    }
}