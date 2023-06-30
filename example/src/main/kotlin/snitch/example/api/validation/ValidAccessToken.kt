package snitch.example.api.validation

import snitch.validation.stringValidator
import snitch.example.security.SecurityModule.jwt

val validAccessToken = stringValidator { jwt().validate(it) }