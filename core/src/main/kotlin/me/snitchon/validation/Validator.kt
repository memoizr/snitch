package me.snitchon.validation

import me.snitchon.parsing.Parser

interface Validator<T, R> {
    val regex: Regex
    val description: String
    val parse: Parser.(Collection<String>) -> R
    fun optional(): Validator<T?, R?> = this as Validator<T?, R?>
}

inline fun <From, To> validator(
    descriptions: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    crossinline mapper: Parser.(String) -> To
) = object : Validator<From, To> {
    override val description = descriptions
    override val regex = regex
    override val parse: Parser.(Collection<String>) -> To = { mapper(it.single()) }
}

inline fun <To> stringValidator(
    description: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    crossinline mapper: Parser.(String) -> To,
) = validator<String, To>(description, regex, mapper)

fun <From, To> validatorMulti(
    descriptions: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    mapper: Parser.(Collection<String>) -> To
) = object : Validator<From, To> {
    override val description = descriptions
    override val regex = regex
    override val parse: Parser.(Collection<String>) -> To = mapper
}
fun <To> stringValidatorMulti(
    description: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    mapper: Parser.(Collection<String>) -> To,
) = validatorMulti<String, To>(description, regex, mapper)
