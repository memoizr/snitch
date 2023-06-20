package me.snitchon.example.api.validation

import com.snitch.me.snitchon.stringValidator
import me.snitchon.example.security.SecurityModule.jwt

val validAccessToken = stringValidator("valid jwt") { jwt().validate(it) }