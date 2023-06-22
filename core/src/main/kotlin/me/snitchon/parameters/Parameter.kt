package me.snitchon.parameters

import com.snitch.me.snitchon.Validator

sealed class Parameter<T, R>(
    open val type: Class<*>,
    open val name: String,
    open val pattern: Validator<T, R>,
    open val description: String,
    open val required: Boolean = false,
    open val emptyAsMissing: Boolean = false,
    open val invalidAsMissing: Boolean = false
)



