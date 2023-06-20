package me.snitchon.example.api.validation

import com.snitch.me.snitchon.Validator
import me.snitchon.example.security.Authentication
import me.snitchon.example.security.SecurityModule.jwt
import me.snitchon.parsing.Parser

object ValidAccessToken : Validator<String, Authentication> {
    override val description = "valid jwt"
    override val regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL)
    override val parse: Parser.(String) -> Authentication = { jwt().validate(it) }
}
