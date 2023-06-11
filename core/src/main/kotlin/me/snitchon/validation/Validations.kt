package com.snitch.me.snitchon
import me.snitchon.parsing.Parser
import kotlin.reflect.KClass
import kotlin.text.RegexOption.*

object NonNegativeInt : Validator<Int, Int> {
    override val description = "non negative integer"
    override val regex = """^\d+$""".toRegex()
    override val parse: Parser.(String) -> Int = { it.toInt() }
}

object NonEmptyString : Validator<String, String> {
    override val description = "non empty string"
    override val regex = """^.+$""".toRegex(DOT_MATCHES_ALL)
    override val parse: Parser.(String) -> String = { it }
}

object NonEmptySingleLineString : Validator<String, String> {
    override val description = "non empty single-line string"
    override val regex = """^.+$""".toRegex()
    override val parse: Parser.(String) -> String = { it }
}

object NonEmptyStringSet : Validator<String, Set<String>> {
    override val description = "non empty string set"
    override val regex = """^(.+,?)*.+$""".toRegex()
    override val parse: Parser.(String) -> Set<String> = { it.split(",").toSet() }
}

object StringSet : Validator<String, Set<String>> {
    override val description = "string set"
    override val regex = """.*""".toRegex()
    override val parse: Parser.(String) -> Set<String> = { it.split(",").toSet() }
}

interface Validator<T, R> {
    val regex: Regex
    val description: String
    val parse: Parser.(String) -> R

    fun optional(): Validator<T?, R?> = this as Validator<T?, R?>
}

class Enum<E : kotlin.Enum<*>>(e: KClass<E>) : Validator<E, E> {
    private val values = e.java.enumConstants.asList().joinToString("|")
    override val description: String = "A string of value: $values"
    override val parse: Parser.(String) -> E = {
        it.parse(e.java)
   }
    override val regex: Regex = "^($values)$".toRegex()
}

inline fun <reified E : kotlin.Enum<*>> enum() = Enum(E::class)
