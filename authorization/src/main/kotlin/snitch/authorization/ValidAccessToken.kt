package snitch.authorization

import snitch.validation.stringValidator

val validAccessToken = stringValidator { SecurityModule.jwt().validate(it) }