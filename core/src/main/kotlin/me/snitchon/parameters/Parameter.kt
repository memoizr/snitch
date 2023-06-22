package me.snitchon.parameters

import com.snitch.me.snitchon.NonEmptyString
import me.snitchon.documentation.Visibility
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

inline fun <reified T, R> optionalQuery(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) =
    OptionalQueryParamDelegate(
        T::class.java,
        name,
        condition.optional(),
        description,
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
) =
    OptionalQueryParamDelegate(
        T::class.java,
        name,
        condition,
        description,
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
    NonEmptyString,
    name,
    description,
    emptyAsMissing,
    invalidAsMissing,
    visibility
)

fun  optionalQuery(
    name: String = "",
    default: String,
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = optionalQuery(
    NonEmptyString,
    name,
    description,
    default,
    emptyAsMissing,
    invalidAsMissing,
    visibility
)

fun query(
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = query(
    NonEmptyString,
    name,
    description,
    emptyAsMissing,
    invalidAsMissing,
    visibility
)

inline fun <reified T, R> query(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) =
    QueryParamDelegate(
        T::class.java,
        name,
        condition,
        description,
        emptyAsMissing = emptyAsMissing,
        invalidAsMissing = invalidAsMissing,
        visibility = visibility
    )

inline fun <reified T, R> optionalHeader(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) =
    OptionalHeaderParamDelegate(
        T::class.java,
        name,
        condition.optional(),
        description,
        default = null,
        emptyAsMissing = emptyAsMissing,
        invalidAsMissing = invalidAsMissing,
        visibility = visibility
    )

fun optionalHeader(
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = optionalHeader(
    NonEmptyString,
    name,
    description,
    emptyAsMissing,
    invalidAsMissing,
    visibility
)

fun  optionalHeader(
    name: String = "",
    default: String,
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = optionalHeader(
    NonEmptyString,
    name,
    description,
    emptyAsMissing,
    default,
    invalidAsMissing,
    visibility
)

inline fun <reified T, R> optionalHeader(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    default: R,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) =
    OptionalHeaderParamDelegate(
        T::class.java,
        name,
        condition,
        description,
        default = default,
        emptyAsMissing = emptyAsMissing,
        invalidAsMissing = invalidAsMissing,
        visibility = visibility
    )

fun header(
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = header(
    NonEmptyString,
    name,
    description,
    emptyAsMissing,
    invalidAsMissing,
    visibility
)

inline fun <reified T, R> header(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = HeaderParamDelegate(
    T::class.java,
    name,
    condition,
    description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> path(condition: Validator<T, R>, name: String = "", description: String = "") =
    PathParamDelegate<T, R>(
        T::class.java,
        name,
        condition,
        description
    )

fun path(name: String = "", description: String = "") =
    path(NonEmptyString, name, description)
