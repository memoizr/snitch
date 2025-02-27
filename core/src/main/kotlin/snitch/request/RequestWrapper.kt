package snitch.request

import snitch.parameters.HeaderParam
import snitch.parameters.HeaderParameter
import snitch.parameters.InvalidParametersException
import snitch.parameters.OptionalHeaderParam
import snitch.parameters.OptionalParam
import snitch.parameters.OptionalQueryParam
import snitch.parameters.Parameter
import snitch.parameters.PathParam
import snitch.parameters.QueryParam
import snitch.parameters.QueryParameter
import snitch.response.CommonResponses
import snitch.types.HTTPMethods
import snitch.types.Parser
import snitch.validation.UnregisteredParamException

/**
 * Core interface for handling HTTP requests in the Snitch framework.
 *
 * RequestWrapper provides a unified interface for accessing and validating request data, including:
 * - Path parameters
 * - Query parameters
 * - Header parameters
 * - Request body
 *
 * It abstracts away the underlying HTTP implementation and provides a type-safe
 * way to access request parameters through property delegates.
 *
 * This interface implements [CommonResponses], which provides convenient methods for
 * creating HTTP responses (e.g., `ok`, `created`, `badRequest`, etc.).
 */
interface RequestWrapper : CommonResponses {
    /**
     * Function that provides access to the parsed request body.
     *
     * @return The parsed request body, or null if no body is present.
     */
    val body: () -> Any?
    
    /**
     * The set of registered parameters for this request.
     *
     * This includes path, query, and header parameters that were registered
     * during route definition.
     */
    val params: Set<Parameter<*, *>>
    
    /**
     * The parser used to deserialize request data.
     */
    val parser: Parser
    
    /**
     * The HTTP method of the request (GET, POST, PUT, DELETE, etc.)
     */
    val method: HTTPMethods
    
    /**
     * The path of the request.
     */
    val path: String
    
    /**
     * Self-reference to the request wrapper.
     *
     * This is a convenience property that allows handlers to use `request`
     * to refer to the request wrapper instance.
     */
    val request get() = this

    /**
     * Gets a path parameter by name.
     *
     * @param name The name of the path parameter.
     * @return The value of the path parameter, or null if not found.
     */
    fun params(name: String): String?
    
    /**
     * Gets header values by name.
     *
     * @param name The name of the header.
     * @return A collection of header values for the given name.
     */
    fun headers(name: String): Collection<String>
    
    /**
     * Gets query parameter values by name.
     *
     * @param name The name of the query parameter.
     * @return A collection of query parameter values for the given name.
     */
    fun queryParams(name: String): Collection<String>
    
    /**
     * Gets the raw value of a path parameter.
     *
     * @param param The path parameter descriptor.
     * @return The raw string value of the path parameter, or null if not found.
     */
    fun getPathParam(param: PathParam<*, *>): String?
    
    /**
     * Gets the raw values of a query parameter.
     *
     * @param param The query parameter descriptor.
     * @return A collection of raw string values for the query parameter, or null if not found.
     */
    fun getQueryParam(param: QueryParameter<*, *>): Collection<String>?
    
    /**
     * Gets the raw values of a header parameter.
     *
     * @param param The header parameter descriptor.
     * @return A collection of raw string values for the header parameter, or null if not found.
     */
    fun getHeaderParam(param: HeaderParameter<*, *>): Collection<String>?

    /**
     * Generates an error message for a missing parameter.
     *
     * @param path The type of parameter (Path, Query, Header).
     * @param it The parameter descriptor.
     * @return A formatted error message.
     */
    private fun missingParameterMessage(path: String, it: Parameter<*, *>) =
        """Required $path parameter `${it.name}` is missing"""

    /**
     * Generates an error message for an invalid parameter.
     *
     * @param query The type of parameter (Path, Query, Header).
     * @param it The parameter descriptor.
     * @param value The invalid value(s).
     * @return A formatted error message.
     */
    private fun invalidParameterMessage(query: String, it: Parameter<*, *>, value: Collection<String>?) =
        """$query parameter `${it.name}` is invalid, expecting ${it.pattern.description}, got `${value?.joinToString(",")}`"""

    /**
     * Validates all registered parameters and returns a list of error messages.
     *
     * This method checks all registered path, query, and header parameters
     * against their validators and returns error messages for any that are
     * invalid or missing.
     *
     * @return A list of error messages for invalid or missing parameters.
     */
    fun getInvalidParams(): List<String> {
        return params
            .map {
                when (it) {
                    is PathParam<*, *> -> validateParam(it, listOf(getPathParam(it).orEmpty()), "Path")
                    is QueryParameter<*, *> -> validateParam(it, getQueryParam(it), "Query")
                    is HeaderParameter<*, *> -> validateParam(it, getHeaderParam(it), "Header")
                }
            }.filterNotNull()
    }

    /**
     * Validates a single parameter and returns an error message if it's invalid.
     *
     * @param it The parameter descriptor.
     * @param value The parameter value(s) to validate.
     * @param path The type of parameter (Path, Query, Header).
     * @return An error message if the parameter is invalid or missing, null otherwise.
     */
    private fun validateParam(it: Parameter<*, *>, value: Collection<String>?, path: String): String? {
        return try {
            when {
                it.required && (value == null || value.isEmpty()) -> missingParameterMessage(path, it)
                !it.required && value == null -> null
                value != null && it.pattern.parse(parser, value).let { true } -> null
                it.pattern.regex.matches(value.toString()) -> null
                else -> invalidParameterMessage(path, it, value)

            }
        } catch (e: Exception) {
            invalidParameterMessage(path, it, value)
        }
    }

    /**
     * Gets a path parameter by its descriptor.
     *
     * This operator function allows for type-safe access to path parameters
     * by using property delegates (e.g., `request[userId]`).
     *
     * @param param The path parameter descriptor.
     * @return The parsed and validated path parameter value.
     * @throws UnregisteredParamException If the parameter is not registered.
     * @throws InvalidParametersException If the parameter is invalid.
     */
    operator fun <T : Any, R> get(param: PathParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam(param) {
                params(param.name)
                    .let { param.pattern.parse(parser, listOf(it.orEmpty())) }
            }!!

    /**
     * Gets a query parameter by its descriptor.
     *
     * This operator function allows for type-safe access to query parameters
     * by using property delegates (e.g., `request[page]`).
     *
     * @param param The query parameter descriptor.
     * @return The parsed and validated query parameter value.
     * @throws UnregisteredParamException If the parameter is not registered.
     * @throws InvalidParametersException If the parameter is invalid.
     */
    operator fun <T : Any?, R> get(param: QueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam(param) {
                queryParams(param.name)
                    .filterValid(param)
                    .let { param.pattern.parse(parser, it.orEmpty()) }
            }!!

    /**
     * Gets an optional query parameter by its descriptor.
     *
     * This operator function allows for type-safe access to optional query parameters
     * by using property delegates (e.g., `request[limit]`).
     *
     * @param param The optional query parameter descriptor.
     * @return The parsed and validated query parameter value, or the default value if not present.
     * @throws UnregisteredParamException If the parameter is not registered.
     */
    operator fun <T : Any?, R> get(param: OptionalQueryParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam(param) {
                queryParams(param.name)
                    .filterValid(param)
                    ?.let { param.pattern.parse(parser, it) }
            } ?: param.default

    /**
     * Gets a header parameter by its descriptor.
     *
     * This operator function allows for type-safe access to header parameters
     * by using property delegates (e.g., `request[contentType]`).
     *
     * @param param The header parameter descriptor.
     * @return The parsed and validated header parameter value.
     * @throws UnregisteredParamException If the parameter is not registered.
     * @throws InvalidParametersException If the parameter is invalid.
     */
    operator fun <T : Any?, R> get(param: HeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam(param) {
                headers(param.name)
                    .let { param.pattern.parse(parser, it) }
            }!!

    /**
     * Gets an optional header parameter by its descriptor.
     *
     * This operator function allows for type-safe access to optional header parameters
     * by using property delegates (e.g., `request[ifNoneMatch]`).
     *
     * @param param The optional header parameter descriptor.
     * @return The parsed and validated header parameter value, or the default value if not present.
     * @throws UnregisteredParamException If the parameter is not registered.
     */
    operator fun <T : Any?, R> get(param: OptionalHeaderParam<T, R>): R =
        checkParamIsRegistered(param)
            .tryParam(param) {
                headers(param.name)
                    .filterValid(param)
                    ?.let { param.pattern.parse(parser, it) }
            } ?: param.default
}

/**
 * Checks if a parameter is registered and throws an exception if not.
 *
 * @param param The parameter to check.
 * @return The RequestWrapper instance if the parameter is registered.
 * @throws UnregisteredParamException If the parameter is not registered.
 */
private inline fun RequestWrapper.checkParamIsRegistered(param: Parameter<*, *>) =
    if (!params.contains(param)) throw UnregisteredParamException(param) else this

/**
 * Attempts to retrieve and parse a parameter value, handling exceptions.
 *
 * @param param The parameter descriptor.
 * @param block The function that retrieves and parses the parameter value.
 * @return The parsed parameter value, or null if the parameter is optional and invalidAsMissing is true.
 * @throws InvalidParametersException If the parameter is invalid and not configured to be treated as missing.
 */
private inline fun <R> RequestWrapper.tryParam(param: Parameter<*, R>, block: () -> R?): R? = try {
    block()
} catch (e: Exception) {
    if (param is OptionalParam<*>&& param.invalidAsMissing) {
        null
    } else throw InvalidParametersException(e, getInvalidParams())
}

/**
 * Filters a collection of parameter values according to the parameter's configuration.
 *
 * @param param The parameter descriptor.
 * @return The filtered collection of values, or null if the collection is empty or all values are empty.
 */
fun Collection<String>.filterValid(param: Parameter<*, *>): Collection<String>? = when {
    this.isEmpty() -> null
    param.emptyAsMissing && this.all { it.isEmpty() } -> null
    else -> this
}

/**
 * Filters a parameter value according to the parameter's configuration.
 *
 * @param param The parameter descriptor.
 * @return The filtered value, or null if the value is null or empty.
 */
fun String?.filterValid(param: Parameter<*, *>): String? = when {
    this == null -> null
    param.emptyAsMissing && this.isEmpty() -> null
    else -> this
}