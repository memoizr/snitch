package snitch.parameters

import snitch.validation.Validator
import snitch.validation.ofNonEmptyString

/**
 * Creates a path parameter delegate with type validation.
 *
 * Path parameters are essential components in URL routing, allowing for dynamic segments in API endpoints.
 * This factory function creates a delegate for strongly-typed path parameters with validation support.
 *
 * @param condition The validator to apply to the path parameter value. This ensures type safety and validates user input.
 * @param name Optional custom name for the parameter. If empty, the property name will be used instead.
 * @param description Optional description for documentation purposes.
 * @param T The input type for validation.
 * @param R The output type after validation.
 * @return A [PathParamDelegate] instance for property delegation.
 *
 * Usage example:
 * ```kotlin
 * // Define a typed path parameter with validation
 * val userId by path(ofNonNegativeInt, description = "User ID for lookup")
 *
 * // Use in route definition
 * GET("users" / userId) isHandledBy { ... }
 * ```
 *
 * The parameter will be accessible in request handlers via `request[userId]`.
 */
inline fun <reified T, R> path(condition: Validator<T, R>, name: String = "", description: String = "") =
    PathParamDelegate(
        type = T::class.java,
        name = name,
        pattern = condition,
        description = description
    )

/**
 * Creates a string path parameter delegate with non-empty validation.
 *
 * This is a convenience function for creating path parameters with string type and non-empty validation.
 * It's a shorthand for `path(ofNonEmptyString, name, description)`.
 *
 * @param name Optional custom name for the parameter. If empty, the property name will be used instead.
 * @param description Optional description for documentation purposes.
 * @return A [PathParamDelegate] instance for property delegation.
 *
 * Usage example:
 * ```kotlin
 * // Define a string path parameter
 * val category by path(description = "Product category")
 *
 * // Use in route definition
 * GET("products" / category) isHandledBy { ... }
 * ```
 *
 * This parameter will be accessible in request handlers via `request[category]`.
 */
fun path(name: String = "", description: String = "") = path(ofNonEmptyString, name, description)