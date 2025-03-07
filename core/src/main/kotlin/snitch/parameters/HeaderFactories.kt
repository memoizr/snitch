package snitch.parameters

import snitch.validation.ofNonEmptyString
import snitch.documentation.Visibility
import snitch.validation.Validator

/**
 * Creates an optional header parameter delegate with type validation.
 *
 * This function creates a delegate for optional HTTP header parameters with custom validation.
 * It allows for type-safe access to header values in request handlers.
 *
 * @param condition The validator to apply to the header parameter value
 * @param name Optional custom name for the header. If empty, the property name will be used
 * @param description Optional description for documentation purposes
 * @param emptyAsMissing When true, empty strings will be treated as if the header is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the header is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @param T The input type for validation
 * @param R The output type after validation
 * @return An [OptionalHeaderParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define an optional custom header parameter
 * val apiVersion by optionalHeader(ofNonEmptyString, description = "API version to use")
 *
 * // Use in route definition
 * GET("endpoint") withHeader apiVersion isHandledBy { ... }
 * ```
 */
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

/**
 * Creates an optional string header parameter delegate.
 *
 * This is a convenience function for creating optional string header parameters
 * with the [ofNonEmptyString] validator. It's a shorthand for
 * `optionalHeader(ofNonEmptyString, name, description, ...)`.
 *
 * @param name Optional custom name for the header. If empty, the property name will be used
 * @param description Optional description for documentation purposes
 * @param emptyAsMissing When true, empty strings will be treated as if the header is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the header is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @return An [OptionalHeaderParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define an optional string header parameter
 * val userAgent by optionalHeader(description = "Client user agent")
 *
 * // Use in route definition
 * GET("endpoint") withHeader userAgent isHandledBy { ... }
 * ```
 */
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

/**
 * Creates an optional string header parameter delegate with a default value.
 *
 * This is a convenience function for creating optional string header parameters
 * with the [ofNonEmptyString] validator and a specified default value.
 *
 * @param name Optional custom name for the header. If empty, the property name will be used
 * @param default The default string value to use when the header is missing
 * @param description Optional description for documentation purposes
 * @param emptyAsMissing When true, empty strings will be treated as if the header is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the header is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @return An [OptionalHeaderParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define an optional header parameter with default value "v1"
 * val apiVersion by optionalHeader(default = "v1", description = "API version to use")
 *
 * // Use in route definition
 * GET("endpoint") withHeader apiVersion isHandledBy { ... }
 * ```
 */
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

/**
 * Creates an optional header parameter delegate with a default value and type validation.
 *
 * This function creates a delegate for optional HTTP header parameters with a specified
 * default value. If the header is missing or invalid (depending on configuration),
 * the default value will be used.
 *
 * @param condition The validator to apply to the header parameter value
 * @param name Optional custom name for the header. If empty, the property name will be used
 * @param description Optional description for documentation purposes
 * @param emptyAsMissing When true, empty strings will be treated as if the header is missing
 * @param default The default value to use when the header is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the header is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @param T The input type for validation
 * @param R The output type after validation
 * @return An [OptionalHeaderParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define an optional integer header parameter with default value 1
 * val maxRetries by optionalHeader(ofNonNegativeInt, default = 3, description = "Maximum retry attempts")
 *
 * // Use in route definition
 * GET("endpoint") withHeader maxRetries isHandledBy { ... }
 * ```
 */
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

/**
 * Creates a required string header parameter delegate.
 *
 * This is a convenience function for creating required string header parameters
 * with the [ofNonEmptyString] validator. It's a shorthand for
 * `header(ofNonEmptyString, name, description, ...)`.
 *
 * @param name Optional custom name for the header. If empty, the property name will be used
 * @param description Optional description for documentation purposes
 * @param emptyAsMissing When true, empty strings will be treated as if the header is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the header is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @return A [HeaderParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define a required string header parameter
 * val authorization by header(description = "Bearer token for authentication")
 *
 * // Use in route definition
 * GET("secure-endpoint") withHeader authorization isHandledBy { ... }
 * ```
 */
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

/**
 * Creates a required header parameter delegate with type validation.
 *
 * This function creates a delegate for required HTTP header parameters that can be
 * used in API endpoints. It allows for type-safe access to header values with
 * robust validation. If the header is missing or invalid, an error response
 * will be returned.
 *
 * @param condition The validator to apply to the header parameter value
 * @param name Optional custom name for the header. If empty, the property name will be used
 * @param description Optional description for documentation purposes
 * @param emptyAsMissing When true, empty strings will be treated as if the header is missing
 * @param invalidAsMissing When true, invalid values will be treated as if the header is missing
 * @param visibility Controls whether the parameter appears in public API documentation
 * @param T The input type for validation
 * @param R The output type after validation
 * @return A [HeaderParamDelegate] instance for property delegation
 *
 * Usage example:
 * ```kotlin
 * // Define a required integer header parameter
 * val contentLength by header(ofNonNegativeInt, description = "Length of the request body in bytes")
 *
 * // Use in route definition
 * POST("upload") withHeader contentLength isHandledBy { ... }
 * ```
 */
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