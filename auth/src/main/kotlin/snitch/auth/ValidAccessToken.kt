package snitch.auth

import snitch.validation.stringValidator

val validAccessToken = stringValidator { SecurityModule.jwt().validate(it) }