package me.snitchon.example.api

import com.snitch.me.snitchon.NonEmptyString
import me.snitchon.example.api.validation.ValidAccessToken
import me.snitchon.parameters.header
import me.snitchon.parameters.path

object Paths {
    val userId = path("userId", condition = NonEmptyString)
}

object Headers {
    val accessToken = header("X-Access-Token", condition = ValidAccessToken)
}