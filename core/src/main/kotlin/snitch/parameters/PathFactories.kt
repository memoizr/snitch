package snitch.parameters

import snitch.validation.Validator
import snitch.validation.ofNonEmptyString

inline fun <reified T, R> path(condition: Validator<T, R>, name: String = "", description: String = "") =
    PathParamDelegate(
        type = T::class.java,
        name = name,
        pattern = condition,
        description = description
    )

fun path(name: String = "", description: String = "") = path(ofNonEmptyString, name, description)
