package me.snitchon.validation

import me.snitchon.parsing.Parser

interface Validator<T, R> {
    val regex: Regex
    val description: String
    val parse: Parser.(String) -> R

    fun optional(): Validator<T?, R?> = this as Validator<T?, R?>
}

fun <From, To> validator(
    descriptions: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL), mapper: Parser.(String) -> To
) = object : Validator<From, To> {
    override val description = descriptions
    override val regex = regex
    override val parse: Parser.(String) -> To = mapper
}

fun <To> stringValidator(
    description: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL), mapper: Parser.(String) -> To
) = object : Validator<String, To> {
    override val description = description
    override val regex = regex
    override val parse: Parser.(String) -> To = mapper
}
