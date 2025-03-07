package snitch.validation

import snitch.types.Parser
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.*

/**
 * Validates non-negative integers
 */
val ofNonNegativeInt = validator<Int, Int>(
    "non negative integer",
    """^\d+$""".toRegex()
) {
    it.toInt().also {
        if (it < 0) throw IllegalArgumentException()
    }
}

/**
 * Validates integers
 */
val ofInt = validator<Int, Int>(
    "integer",
    """^-?\d+$""".toRegex()
) {
    it.toInt()
}

/**
 * Validates integers within a specific range
 */
fun ofIntRange(min: Int, max: Int) = validator<Int, Int>(
    "integer between $min and $max",
    """^-?\d+$""".toRegex()
) {
    it.toInt().also {
        if (it < min || it > max) throw IllegalArgumentException("Value must be between $min and $max")
    }
}

/**
 * Validates positive integers (greater than zero)
 */
val ofPositiveInt = validator<Int, Int>(
    "positive integer",
    """^[1-9]\d*$""".toRegex()
) {
    it.toInt().also {
        if (it <= 0) throw IllegalArgumentException("Value must be greater than 0")
    }
}

/**
 * Validates double values
 */
val ofDouble = validator<Double, Double>(
    "double",
    """^-?\d+(\.\d+)?$""".toRegex()
) {
    it.toDouble()
}

/**
 * Validates double values within a specific range
 */
fun ofDoubleRange(min: Double, max: Double) = validator<Double, Double>(
    "double between $min and $max",
    """^-?\d+(\.\d+)?$""".toRegex()
) {
    it.toDouble().also {
        if (it < min || it > max) throw IllegalArgumentException("Value must be between $min and $max")
    }
}

/**
 * Validates boolean values (true/false, yes/no, 1/0)
 */
val ofBoolean = validator<Boolean, Boolean>(
    "boolean (true/false, yes/no, 1/0)",
    """^(true|false|yes|no|1|0)$""".toRegex(RegexOption.IGNORE_CASE)
) {
    when (it.lowercase(Locale.getDefault())) {
        "true", "yes", "1" -> true
        "false", "no", "0" -> false
        else -> throw IllegalArgumentException("Invalid boolean value")
    }
}

/**
 * Validates email addresses
 */
val ofEmail = validator<String, String>(
    "email address",
    """^[a-zA-Z0-9][a-zA-Z0-9._%+-]*[a-zA-Z0-9]@[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9](\.[a-zA-Z0-9][a-zA-Z0-9-]*[a-zA-Z0-9])+$""".toRegex()
) {
    it
}

/**
 * Validates URLs
 */
val ofUrl = validator<String, URI>(
    "URL",
    """^(https?|ftp)://[^\s/$.?#].[^\s]*$""".toRegex()
) {
    try {
        URI.create(it)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid URL format")
    }
}

/**
 * Validates IPv4 addresses
 */
val ofIpv4 = validator<String, String>(
    "IPv4 address",
    """^((25[0-5]|(2[0-4]|1\d|[1-9]|)\d)\.?\b){4}$""".toRegex()
) {
    it
}

/**
 * Validates ISO-8601 dates (YYYY-MM-DD)
 */
val ofDate = validator<String, LocalDate>(
    "date in ISO-8601 format (YYYY-MM-DD)",
    """^\d{4}-\d{2}-\d{2}$""".toRegex()
) {
    try {
        LocalDate.parse(it)
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException("Invalid date format, expected YYYY-MM-DD")
    }
}

/**
 * Validates ISO-8601 datetimes (YYYY-MM-DDThh:mm:ss)
 */
val ofDateTime = object : Validator<String, LocalDateTime> {
    override val description = "datetime in ISO-8601 format (YYYY-MM-DDThh:mm:ss)"
    override val regex = """^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$""".toRegex()
    override val parse: Parser.(Collection<String>) -> LocalDateTime = { collection ->
        if (collection.isEmpty()) {
            throw IllegalArgumentException("Empty collection for datetime")
        }
        if (collection.size > 1) {
            throw IllegalArgumentException("Multiple values provided for datetime")
        }
        try {
            LocalDateTime.parse(collection.first())
        } catch (e: DateTimeParseException) {
            throw IllegalArgumentException("Invalid datetime format, expected YYYY-MM-DDThh:mm:ss")
        }
    }
}

/**
 * Validates custom date format
 */
fun ofDateFormat(format: String) = validator<String, LocalDate>(
    "date in format: $format",
    """^.+$""".toRegex()
) {
    try {
        LocalDate.parse(it, DateTimeFormatter.ofPattern(format))
    } catch (e: DateTimeParseException) {
        throw IllegalArgumentException("Invalid date format, expected $format")
    }
}

/**
 * Validates UUIDs
 */
val ofUuid = validator<String, UUID>(
    "UUID",
    """^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$""".toRegex(RegexOption.IGNORE_CASE)
) {
    try {
        UUID.fromString(it)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid UUID format")
    }
}

/**
 * Validates alphanumeric strings
 */
val ofAlphanumeric = validator<String, String>(
    "alphanumeric string",
    """^[a-zA-Z0-9]+$""".toRegex()
) {
    it
}

/**
 * Validates phone numbers
 */
val ofPhoneNumber = validator<String, String>(
    "phone number (digits, spaces, and +)",
    """^\+?[\d\s]+$""".toRegex()
) {
    it.replace("\\s".toRegex(), "")  // Remove spaces
}

/**
 * Validates string length within specified range
 */
fun ofStringLength(minLength: Int, maxLength: Int) = validator<String, String>(
    "string with length between $minLength and $maxLength characters",
    """^.{$minLength,$maxLength}$""".toRegex(RegexOption.DOT_MATCHES_ALL)
) {
    if (it.length < minLength || it.length > maxLength) {
        throw IllegalArgumentException("String length must be between $minLength and $maxLength characters")
    }
    it
}

/**
 * Validates string with a custom regex pattern
 */
fun ofRegexPattern(pattern: String, description: String) = validator<String, String>(
    description,
    pattern.toRegex()
) {
    it
}

/**
 * Validates JSON strings
 */
val ofJson = validator<String, String>(
    "valid JSON string",
    """^[\[\{\"].*[\]\}\"]$""".toRegex(RegexOption.DOT_MATCHES_ALL)
) {
    // This is a very basic check - in production you'd want to use a JSON parser
    if (!((it.startsWith("{") && it.endsWith("}")) ||
          (it.startsWith("[") && it.endsWith("]")) ||
          (it.startsWith("\"") && it.endsWith("\"")))
    ) {
        throw IllegalArgumentException("Invalid JSON format")
    }
    it
}

/**
 * Validates non-empty strings
 */
val ofNonEmptyString = stringValidator("non empty string") {
    if (it.isEmpty()) throw IllegalArgumentException()
    else it
}

/**
 * Validates non-empty single-line strings
 */
val ofNonEmptySingleLineString = stringValidator("non empty single-line string") {
    if (it.isEmpty() || it.lines().size != 1)
        throw IllegalArgumentException()
    else it
}

/**
 * Validates non-empty sets of strings
 */
val ofNonEmptyStringSet = stringValidatorMulti("non empty string set") {
    it.flatMap { it.split(",") }
        .filter { it.isNotEmpty() }
        .ifEmpty { throw ValidationException(it) }
        .toSet()
}

/**
 * Validates sets of strings (can be empty)
 */
val ofStringSet = stringValidatorMulti("string set") {
    it.flatMap { it.split(",") }.toSet()
}

/**
 * Validates a single enum value
 */
inline fun <reified E : Enum<*>> ofEnum(): Validator<String, E> {
    val e = E::class
    val values = e.java.enumConstants.asList().joinToString("|")
    val regex: Regex = "^($values)$".toRegex()
    val description = "A string of value: $values"
    return validator(description, regex) {
        it.parse(e.java)
    }
}

/**
 * Validates a collection of enum values
 */
inline fun <reified E : Enum<*>> ofRepeatableEnum(): Validator<String, Collection<E>> {
    val e = E::class
    val values = e.java.enumConstants.asList().joinToString("|")
    val regex: Regex = "^($values)$".toRegex()
    val description = "A string of value: $values"
    return validatorMulti(description, regex) {
        it.flatMap { it.split(",") }
            .map { it.parse(e.java) }
    }
}
