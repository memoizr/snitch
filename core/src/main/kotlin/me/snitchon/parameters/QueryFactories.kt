package me.snitchon.parameters

import com.snitch.me.snitchon.NonEmptyString
import com.snitch.me.snitchon.Validator
import me.snitchon.documentation.Visibility

inline fun <reified T, R> optionalQuery(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = OptionalQueryParamDelegate(
    type = T::class.java,
    name = name,
    pattern = condition.optional(),
    description = description,
    default = null,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> optionalQuery(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    default: R,
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = OptionalQueryParamDelegate(
    type = T::class.java,
    name = name,
    pattern = condition,
    description = description,
    default = default,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

fun optionalQuery(
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = optionalQuery(
    condition = NonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

fun optionalQuery(
    name: String = "",
    default: String,
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = optionalQuery(
    condition = NonEmptyString,
    name = name,
    description = description,
    default = default,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

fun query(
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = query(
    condition = NonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> query(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = QueryParamDelegate(
    type = T::class.java,
    name = name,
    pattern = condition,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)
