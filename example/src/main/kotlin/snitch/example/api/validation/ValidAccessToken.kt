package snitch.example.api.validation

import me.snitch.validation.stringValidator
import snitch.example.security.SecurityModule.jwt

val validAccessToken = stringValidator { jwt().validate(it) }