package com.snitch.me.snitchon

import me.snitchon.validation.*

val ofNonNegativeInt = validator<Int, Int>(
    "non negative integer",
    """^\d+$""".toRegex()
) {
    it.toInt().also {
        if (it < 0) throw IllegalArgumentException()
    }
}

val ofNonEmptyString = stringValidator("non empty string") {
    if (it.isEmpty()) throw IllegalArgumentException()
    else it
}

val ofNonEmptySingleLineString = stringValidator("non empty single-line string") {
    if (it.isEmpty() || it.lines().size != 1)
        throw IllegalArgumentException()
    else it
}

val ofNonEmptyStringSet = stringValidatorMulti("non empty string set") {
    it.flatMap { it.split(",") }
        .filter { it.isNotEmpty() }
        .ifEmpty { throw ValidationException(it) }
        .toSet()
}

val ofStringSet = stringValidatorMulti("string set") {
    it.flatMap { it.split(",") }.toSet()
}

inline fun <reified E : Enum<*>> ofEnum(): Validator<String, E> {
    val e = E::class
    val values = e.java.enumConstants.asList().joinToString("|")
    val regex: Regex = "^($values)$".toRegex()
    val description = "A string of value: $values"
    return validator(description, regex) {
        it.parse(e.java)
    }
}

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
