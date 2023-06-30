package me.snitch.parameters

import com.snitch.me.snitchon.ofNonEmptyString
import me.snitch.documentation.Visibility
import me.snitch.validation.Validator

inline fun <reified T, R> optionalHeader(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) =
    OptionalHeaderParamDelegate(
        type = T::class.java,
        name = name,
        pattern = condition.optional(),
        description = description,
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
): OptionalHeaderParamDelegate<String?, String?> = optionalHeader(
    condition = ofNonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

fun optionalHeader(
    name: String = "",
    default: String,
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
): OptionalHeaderParamDelegate<String, String> = optionalHeader(
    condition = ofNonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    default = default,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> optionalHeader(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    default: R,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = OptionalHeaderParamDelegate(
    type = T::class.java,
    name = name,
    pattern = condition,
    description = description,
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
    condition = ofNonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> header(
    condition: Validator<T, R>,
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = HeaderParamDelegate(
    type = T::class.java,
    name = name,
    pattern = condition,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)