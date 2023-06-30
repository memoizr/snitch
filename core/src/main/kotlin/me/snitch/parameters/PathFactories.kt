package me.snitch.parameters

import com.snitch.me.snitchon.ofNonEmptyString
import me.snitch.validation.Validator


inline fun <reified T, R> path(condition: Validator<T, R>, name: String = "", description: String = "") =
    PathParamDelegate(
        type = T::class.java,
        name = name,
        pattern = condition,
        description = description
    )

fun path(name: String = "", description: String = "") = path(ofNonEmptyString, name, description)
