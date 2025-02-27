package snitch.validation

import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import snitch.types.Parser

class ValidatorsTest {

    private val mockParser = mockk<Parser>()

    @Test
    fun `ofNonNegativeInt should accept valid non-negative integers`() {
        // Valid inputs
        assertTrue(ofNonNegativeInt.regex.matches("0"))
        assertTrue(ofNonNegativeInt.regex.matches("1"))
        assertTrue(ofNonNegativeInt.regex.matches("42"))
        assertTrue(ofNonNegativeInt.regex.matches("999999"))

        // Test parsing
        assertEquals(0, ofNonNegativeInt.parse(mockParser, listOf("0")))
        assertEquals(1, ofNonNegativeInt.parse(mockParser, listOf("1")))
        assertEquals(999999, ofNonNegativeInt.parse(mockParser, listOf("999999")))
    }

    @Test
    fun `ofNonNegativeInt should reject invalid inputs`() {
        // Invalid inputs - should not match regex
        assertFalse(ofNonNegativeInt.regex.matches(""))
        assertFalse(ofNonNegativeInt.regex.matches("-1"))
        assertFalse(ofNonNegativeInt.regex.matches("1.5"))
        assertFalse(ofNonNegativeInt.regex.matches("text"))
        assertFalse(ofNonNegativeInt.regex.matches("1a"))
        
        // These would match the regex but throw exceptions during parsing
        assertThrows<IllegalArgumentException> {
            // Integer.MAX_VALUE + 1 should throw
            ofNonNegativeInt.parse(mockParser, listOf("2147483648"))
        }
    }

    @Test
    fun `ofNonEmptyString should accept valid non-empty strings`() {
        // Valid inputs
        assertTrue(ofNonEmptyString.regex.matches("a"))
        assertTrue(ofNonEmptyString.regex.matches("hello"))
        assertTrue(ofNonEmptyString.regex.matches("line with\nnewline"))
        assertTrue(ofNonEmptyString.regex.matches("12345"))

        // Test parsing
        assertEquals("a", ofNonEmptyString.parse(mockParser, listOf("a")))
        assertEquals("hello", ofNonEmptyString.parse(mockParser, listOf("hello")))
        assertEquals("line with\nnewline", ofNonEmptyString.parse(mockParser, listOf("line with\nnewline")))
    }

    @Test
    fun `ofNonEmptyString should reject empty strings`() {
        // The regex would actually match an empty string due to DOT_MATCHES_ALL
        // But the validation happens in the mapper function
        assertThrows<IllegalArgumentException> {
            ofNonEmptyString.parse(mockParser, listOf(""))
        }
    }

    @Test
    fun `ofNonEmptySingleLineString should accept valid single-line strings`() {
        // Valid inputs
        assertTrue(ofNonEmptySingleLineString.regex.matches("a"))
        assertTrue(ofNonEmptySingleLineString.regex.matches("hello"))
        assertTrue(ofNonEmptySingleLineString.regex.matches("12345"))

        // Test parsing
        assertEquals("a", ofNonEmptySingleLineString.parse(mockParser, listOf("a")))
        assertEquals("hello", ofNonEmptySingleLineString.parse(mockParser, listOf("hello")))
        assertEquals("12345", ofNonEmptySingleLineString.parse(mockParser, listOf("12345")))
    }

    @Test
    fun `ofNonEmptySingleLineString should reject invalid inputs`() {
        // These would match the regex but throw exceptions during parsing
        assertThrows<IllegalArgumentException> {
            ofNonEmptySingleLineString.parse(mockParser, listOf(""))
        }
        
        assertThrows<IllegalArgumentException> {
            ofNonEmptySingleLineString.parse(mockParser, listOf("line with\nnewline"))
        }
    }

    @Test
    fun `ofNonEmptyStringSet should handle valid string sets`() {
        // Valid inputs - should match regex
        assertTrue(ofNonEmptyStringSet.regex.matches("value"))
        assertTrue(ofNonEmptyStringSet.regex.matches("value1,value2"))
        
        // Test parsing
        assertEquals(setOf("value"), ofNonEmptyStringSet.parse(mockParser, listOf("value")))
        assertEquals(setOf("value1", "value2"), ofNonEmptyStringSet.parse(mockParser, listOf("value1,value2")))
        
        // Test with multiple strings in collection
        assertEquals(
            setOf("value1", "value2", "value3"), 
            ofNonEmptyStringSet.parse(mockParser, listOf("value1", "value2,value3"))
        )
        
        // Test with comma and empty string (empty parts should be filtered)
        assertEquals(setOf("value"), ofNonEmptyStringSet.parse(mockParser, listOf("value,")))
    }

    @Test
    fun `ofNonEmptyStringSet should reject empty sets`() {
        // These would match the regex but throw exceptions during parsing        
        assertThrows<ValidationException> {
            ofNonEmptyStringSet.parse(mockParser, listOf(""))
        }
        
        assertThrows<ValidationException> {
            ofNonEmptyStringSet.parse(mockParser, listOf(","))
        }
        
        // Empty collection
        assertThrows<ValidationException> {
            ofNonEmptyStringSet.parse(mockParser, listOf())
        }
    }

    @Test
    fun `ofStringSet should handle all string sets including empty ones`() {
        // Valid inputs - should match regex
        assertTrue(ofStringSet.regex.matches("value"))
        assertTrue(ofStringSet.regex.matches("value1,value2"))
        assertFalse(ofStringSet.regex.matches(""))
        
        // Test parsing
        assertEquals(setOf("value"), ofStringSet.parse(mockParser, listOf("value")))
        assertEquals(setOf("value1", "value2"), ofStringSet.parse(mockParser, listOf("value1,value2")))
        
        // Test with multiple strings in collection
        assertEquals(
            setOf("value1", "value2", "value3"), 
            ofStringSet.parse(mockParser, listOf("value1", "value2,value3"))
        )
        
        // Test with empty parts
        assertEquals(setOf("value", ""), ofStringSet.parse(mockParser, listOf("value,")))
        
        // Test with empty string
        assertEquals(setOf(""), ofStringSet.parse(mockParser, listOf("")))
        
        // Test with empty collection
        assertEquals(emptySet<String>(), ofStringSet.parse(mockParser, listOf()))
    }

    enum class TestEnum { ONE, TWO, THREE }
    
    @Test
    fun `ofEnum should validate enum values correctly`() {
        val enumValidator = ofEnum<TestEnum>()
        
        // Valid inputs - should match regex
        assertTrue(enumValidator.regex.matches("ONE"))
        assertTrue(enumValidator.regex.matches("TWO"))
        assertTrue(enumValidator.regex.matches("THREE"))
        
        // Invalid inputs - should not match regex
        assertFalse(enumValidator.regex.matches("FOUR"))
        assertFalse(enumValidator.regex.matches("one"))
        assertFalse(enumValidator.regex.matches(""))
        
        // Description should list all valid values
        assertTrue(enumValidator.description.contains("ONE|TWO|THREE"))
    }
    
    @Test
    fun `ofRepeatableEnum should validate collections of enum values`() {
        val repeatableEnumValidator = ofRepeatableEnum<TestEnum>()
        
        // Valid inputs - should match regex
        assertTrue(repeatableEnumValidator.regex.matches("ONE"))
        assertTrue(repeatableEnumValidator.regex.matches("TWO"))
        assertTrue(repeatableEnumValidator.regex.matches("THREE"))
        
        // Invalid inputs - should not match regex
        assertFalse(repeatableEnumValidator.regex.matches("FOUR"))
        assertFalse(repeatableEnumValidator.regex.matches("one"))
        assertFalse(repeatableEnumValidator.regex.matches(""))
        
        // Description should list all valid values
        assertTrue(repeatableEnumValidator.description.contains("ONE|TWO|THREE"))
    }
    
    @Test
    fun `stringValidator should create validators with custom logic`() {
        // Create a custom validator for even numbers
        val evenNumberValidator = stringValidator("even number", """^\d+$""".toRegex()) {
            val num = it.toInt()
            if (num % 2 != 0) throw IllegalArgumentException("Not an even number")
            num
        }
        
        // Valid inputs
        assertTrue(evenNumberValidator.regex.matches("2"))
        assertTrue(evenNumberValidator.regex.matches("42"))
        
        // Invalid by regex
        assertFalse(evenNumberValidator.regex.matches("3a"))
        
        // Valid parsing
        assertEquals(2, evenNumberValidator.parse(mockParser, listOf("2")))
        assertEquals(42, evenNumberValidator.parse(mockParser, listOf("42")))
        
        // Invalid by logic
        assertThrows<IllegalArgumentException> {
            evenNumberValidator.parse(mockParser, listOf("3"))
        }
    }
    
    @Test
    fun `stringValidatorMulti should create validators with custom collection logic`() {
        // Create a custom validator that sums a collection of numbers
        val sumValidator = stringValidatorMulti("sum of numbers", """^\d+$""".toRegex()) {
            it.flatMap { it.split(",") }
                .map { it.toInt() }
                .sum()
        }
        
        // Valid inputs
        assertTrue(sumValidator.regex.matches("1"))
        assertTrue(sumValidator.regex.matches("42"))
        
        // Invalid by regex
        assertFalse(sumValidator.regex.matches("3a"))
        
        // Test parsing
        assertEquals(6, sumValidator.parse(mockParser, listOf("1,2,3")))
        assertEquals(10, sumValidator.parse(mockParser, listOf("5", "5")))
        assertEquals(15, sumValidator.parse(mockParser, listOf("5,5", "5")))
    }
    
    @Test
    fun `validator optional method should work correctly`() {
        val optionalNonNegativeInt = ofNonNegativeInt.optional()
        
        // Should still validate the same way for non-null values
        assertEquals(42, optionalNonNegativeInt.parse(mockParser, listOf("42")))
        
        // Should throw for invalid values
        assertThrows<IllegalArgumentException> {
            optionalNonNegativeInt.parse(mockParser, listOf("-1"))
        }
    }
}