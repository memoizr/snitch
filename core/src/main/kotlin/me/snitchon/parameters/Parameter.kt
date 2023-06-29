package me.snitchon.parameters

import me.snitchon.validation.Validator

sealed class Parameter<T, R>(
    open val type: Class<*>,
    open val name: String,
    open val pattern: Validator<T, R>,
    open val description: String,
    open val required: Boolean,
    open val emptyAsMissing: Boolean,
    open val invalidAsMissing: Boolean,
)



