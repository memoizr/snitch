package com.snitch.me.snitchon

import me.snitchon.parsing.Parser
import me.snitchon.validation.Validator
import me.snitchon.validation.stringValidator
import me.snitchon.validation.validator
import kotlin.reflect.KClass

val ofNonNegativeInt = validator<Int, Int>("non negative integer",
    """^\d+$""".toRegex()) {
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

val ofNonEmptyStringSet = stringValidator("non empty string set") {
    it.split(",").toSet()
}

val ofStringSet = stringValidator("string set") {
    it.split(",").toSet()
}


class Enum<E : kotlin.Enum<*>>(e: KClass<E>) : Validator<E, E> {
    private val values = e.java.enumConstants.asList().joinToString("|")
    override val description: String = "A string of value: $values"
    override val parse: Parser.(String) -> E = {
        it.parse(e.java)
    }
    override val regex: Regex = "^($values)$".toRegex()
}

inline fun <reified E : kotlin.Enum<*>> ofEnum() = Enum(E::class)

