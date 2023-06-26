package me.snitchon.example.api.validation

import me.snitchon.example.security.SecurityModule.jwt
import me.snitchon.validation.stringValidator

val validAccessToken = stringValidator { jwt().validate(it) }