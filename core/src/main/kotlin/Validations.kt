package com.snitch
import com.google.gson.Gson
import kotlin.reflect.KClass
import kotlin.text.RegexOption.*

object NonNegativeInt : Validator<Int> {
    override val description = "non negative integer"
    override val regex = """^\d+$""".toRegex()
    override val parse: (String) -> Int = { it.toInt() }
}

object NonEmptyString : Validator<String> {
    override val description = "non empty string"
    override val regex = """^.+$""".toRegex(DOT_MATCHES_ALL)
    override val parse: (String) -> String = { it }
}

object NonEmptySingleLineString : Validator<String> {
    override val description = "non empty single-line string"
    override val regex = """^.+$""".toRegex()
    override val parse: (String) -> String = { it }
}

object NonEmptyStringSet : Validator<Set<String>> {
    override val description = "non empty string set"
    override val regex = """^(.+,?)*.+$""".toRegex()
    override val parse: (String) -> Set<String> = { it.split(",").toSet() }
}

object StringSet : Validator<Set<String>> {
    override val description = "string set"
    override val regex = """.*""".toRegex()
    override val parse: (String) -> Set<String> = { it.split(",").toSet() }
}

interface Validator<T> {
    val regex: Regex
    val description: String

    val parse: (String) -> T

    fun optional(): Validator<T?> = this as Validator<T?>
}

class Enum<E : kotlin.Enum<*>>(e: KClass<E>) : Validator<E> {
    private val values = e.java.enumConstants.asList().joinToString("|")
    override val description: String = "A string of value: $values"
    override val parse: (String) -> E = { Gson().fromJson(it, e.java) }
    override val regex: Regex = "^($values)$".toRegex()
}

inline fun <reified E : kotlin.Enum<*>> enum() = Enum(E::class)
