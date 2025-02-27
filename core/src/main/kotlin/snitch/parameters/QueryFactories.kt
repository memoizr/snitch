package snitch.parameters

import snitch.validation.ofNonEmptyString
import snitch.documentation.Visibility
import snitch.validation.Validator

/**
 * Creates an optional query parameter delegate with type validation.
 *
 * This function creates a delegate for optional query parameters that can be
 * used in API endpoints. It allows for type-safe access to query parameters with
 * robust validation.
 *
 * @param condition The validator to apply to the query parameter value
 * @param name Optional custom name for the parameter. If empty, the property name will be used
 * @param description Optional description for documentation purposes
 * @param emptyAsMissing When true, empty strings will be treated as if the parameter is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the parameter is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @param T The input type for validation
 * @param R The output type after validation
 * @return An [OptionalQueryParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define an optional integer query parameter
 * val page by optionalQuery(ofNonNegativeInt, description = "Page number")
 *
 * // Use in route definition
 * GET("products") withQuery page isHandledBy { ... }
 * ```
 */
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

/**
 * Creates an optional query parameter delegate with a default value.
 *
 * This function creates a delegate for optional query parameters with a specified
 * default value. If the parameter is missing or invalid (depending on configuration),
 * the default value will be used.
 *
 * @param condition The validator to apply to the query parameter value
 * @param name Optional custom name for the parameter. If empty, the property name will be used
 * @param description Optional description for documentation purposes
 * @param default The default value to use when the parameter is missing
 * @param emptyAsMissing When true, empty strings will be treated as if the parameter is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the parameter is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @param T The input type for validation
 * @param R The output type after validation
 * @return An [OptionalQueryParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define an optional integer query parameter with default value 1
 * val page by optionalQuery(ofNonNegativeInt, default = 1, description = "Page number")
 *
 * // Use in route definition
 * GET("products") withQuery page isHandledBy { ... }
 * ```
 */
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

/**
 * Creates an optional string query parameter delegate.
 *
 * This is a convenience function for creating optional string query parameters
 * with the [ofNonEmptyString] validator. It's a shorthand for
 * `optionalQuery(ofNonEmptyString, name, description, ...)`.
 *
 * @param name Optional custom name for the parameter. If empty, the property name will be used
 * @param description Optional description for documentation purposes
 * @param emptyAsMissing When true, empty strings will be treated as if the parameter is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the parameter is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @return An [OptionalQueryParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define an optional string query parameter
 * val search by optionalQuery(description = "Search term")
 *
 * // Use in route definition
 * GET("products") withQuery search isHandledBy { ... }
 * ```
 */
fun optionalQuery(
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = optionalQuery(
    condition = ofNonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

/**
 * Creates an optional string query parameter delegate with a default value.
 *
 * This is a convenience function for creating optional string query parameters
 * with the [ofNonEmptyString] validator and a specified default value.
 *
 * @param name Optional custom name for the parameter. If empty, the property name will be used
 * @param default The default string value to use when the parameter is missing
 * @param description Optional description for documentation purposes
 * @param emptyAsMissing When true, empty strings will be treated as if the parameter is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the parameter is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @return An [OptionalQueryParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define an optional string query parameter with default value "all"
 * val category by optionalQuery(default = "all", description = "Product category")
 *
 * // Use in route definition
 * GET("products") withQuery category isHandledBy { ... }
 * ```
 */
fun optionalQuery(
    name: String = "",
    default: String,
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = optionalQuery(
    condition = ofNonEmptyString,
    name = name,
    description = description,
    default = default,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

/**
 * Creates a required string query parameter delegate.
 *
 * This is a convenience function for creating required string query parameters
 * with the [ofNonEmptyString] validator. It's a shorthand for
 * `query(ofNonEmptyString, name, description, ...)`.
 *
 * @param name Optional custom name for the parameter. If empty, the property name will be used
 * @param description Optional description for documentation purposes
 * @param emptyAsMissing When true, empty strings will be treated as if the parameter is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the parameter is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @return A [QueryParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define a required string query parameter
 * val search by query(description = "Search term")
 *
 * // Use in route definition
 * GET("products") withQuery search isHandledBy { ... }
 * ```
 */
fun query(
    name: String = "",
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = query(
    condition = ofNonEmptyString,
    name = name,
    description = description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

/**
 * Creates a required query parameter delegate with type validation.
 *
 * This function creates a delegate for required query parameters that can be
 * used in API endpoints. It allows for type-safe access to query parameters with
 * robust validation. If the parameter is missing or invalid, an error response
 * will be returned.
 *
 * @param condition The validator to apply to the query parameter value
 * @param name Optional custom name for the parameter. If empty, the property name will be used
 * @param description Optional description for documentation purposes
 * @param emptyAsMissing When true, empty strings will be treated as if the parameter is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the parameter is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @param T The input type for validation
 * @param R The output type after validation
 * @return A [QueryParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define a required integer query parameter
 * val limit by query(ofNonNegativeInt, description = "Number of results to return")
 *
 * // Use in route definition
 * GET("products") withQuery limit isHandledBy { ... }
 * ```
 */
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