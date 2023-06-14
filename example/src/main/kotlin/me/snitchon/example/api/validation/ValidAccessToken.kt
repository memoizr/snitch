package me.snitchon.example.api.validation

import com.snitch.me.snitchon.Validator
import me.snitchon.example.security.verifyJWT
import me.snitchon.example.types.UserId
import me.snitchon.parsing.Parser

object ValidAccessToken : Validator<String, UserId> {
    override val description = "valid jwt"
    override val regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL)
    override val parse: Parser.(String) -> UserId = {
        UserId(verifyJWT(it).body.get("userId", String::class.java))
    }
}