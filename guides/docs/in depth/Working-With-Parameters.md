# Working with Parameters in Snitch

This in-depth guide explores Snitch's parameter handling system, covering advanced topics, implementation details, and best practices for building robust, type-safe APIs.

## Parameter System Architecture

Snitch's parameter system is built on Kotlin's property delegation pattern, providing both compile-time and runtime safety. The system consists of several key components:

1. **Parameter Definitions**: Type-safe property delegates that define parameters
2. **Validators**: Functions that validate and transform parameter values
3. **Parameter Registry**: Keeps track of all parameters used in routes
4. **Parameter Extraction**: Pulls parameter values from HTTP requests
5. **Parameter Access**: Type-safe access to parameter values in handlers

## Parameter Delegates

Each parameter type is implemented as a property delegate:

- `path()`: Path parameters embedded in URL segments
- `query()`: Query parameters from the URL query string
- `header()`: Parameters from HTTP headers
- `body<T>()`: Type-safe access to the request body

Each delegate creates a parameter definition that is registered with the framework when routes are defined.

## Parameter Definition Internals

Let's look at how parameters are defined under the hood:

```kotlin
// PathParam implementation (simplified)
class PathParam<T : Any, C : Any>(
    override val name: String,
    override val validator: ValueValidator<T, C>,
    override val description: String,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: ParameterVisibility,
    override val condition: ValueCondition<C>?
) : Parameter<T, C>
```

The `Parameter<T, C>` interface defines common properties and behaviors for all parameter types:

- `name`: The parameter name used in documentation and error messages
- `validator`: Validates and transforms the parameter string value to type T
- `description`: Human-readable description for documentation
- `emptyAsMissing`: Controls whether empty values are treated as missing
- `invalidAsMissing`: Controls whether invalid values are treated as missing
- `visibility`: Controls whether parameter appears in documentation
- `condition`: Additional validation logic for the parameter

## Parameter Registration and Resolution

When a route is defined, its parameters are registered with the framework:

```kotlin
val userId by path(ofInt)

// The route definition registers the userId parameter
GET("users" / userId) isHandledBy { ... }
```

During request handling:

1. Parameters are extracted from the request
2. Each parameter is validated using its validator
3. Additional conditions are checked
4. If validation fails, an appropriate error response is returned
5. If successful, the handler is invoked with access to the validated parameters

## Advanced Parameter Usage

### Custom Validator Implementation

Creating a custom validator involves implementing the `ValueValidator<T, C>` interface:

```kotlin
// Custom UUID validator implementation
val ofUUID = object : ValueValidator<String, UUID> {
    override val invalidValueErrorMessage = "Invalid UUID format"
    
    override fun validate(value: String): UUID {
        try {
            return UUID.fromString(value)
        } catch (e: IllegalArgumentException) {
            throw InvalidParameterException(invalidValueErrorMessage)
        }
    }
    
    override fun serialize(value: UUID): String {
        return value.toString()
    }
}

// Usage
val userId by path(ofUUID, description = "User ID in UUID format")
```

For simpler validators, you can use the `validator` helper function:

```kotlin
val ofUUID = validator { input ->
    try {
        UUID.fromString(input)
        input
    } catch (e: IllegalArgumentException) {
        throw InvalidParameterException("Invalid UUID format")
    }
}
```

### Parameter Conditions

Conditions provide additional validation for parameters beyond basic type conversion:

```kotlin
// Condition implementation
val positiveNumber = condition<Int> { value ->
    if (value <= 0) {
        ConditionResult.Failure("Value must be positive")
    } else {
        ConditionResult.Success
    }
}

// Usage with a parameter
val quantity by query(ofInt, condition = positiveNumber)
```

Conditions are evaluated after the parameter has been successfully validated and converted to its target type.

### Parameter Visibility

Control whether parameters appear in documentation:

```kotlin
// Public parameter visible in documentation
val apiKey by header(visibility = ParameterVisibility.PUBLIC)

// Internal parameter hidden from documentation
val internalId by header(visibility = ParameterVisibility.INTERNAL)
```

### Parameter Grouping and Organization

For better code organization, group related parameters in objects:

```kotlin
object UserParameters {
    val id by path(ofInt, description = "User ID")
    val email by query(ofEmail, description = "User email")
    val status by optionalQuery(
        validator { input -> UserStatus.valueOf(input.uppercase()) },
        default = UserStatus.ACTIVE,
        description = "User status"
    )
}

object PaginationParameters {
    val page by optionalQuery(ofNonNegativeInt, default = 1, description = "Page number")
    val size by optionalQuery(ofNonNegativeInt, default = 20, description = "Page size")
    val sort by optionalQuery(description = "Sort field")
    val direction by optionalQuery(
        validator { input -> SortDirection.valueOf(input.uppercase()) },
        default = SortDirection.ASC,
        description = "Sort direction"
    )
}
```

This approach makes route definitions more readable and groups related parameters logically:

```kotlin
GET("users") withQuery PaginationParameters.page withQuery PaginationParameters.size isHandledBy {
    val page = request[PaginationParameters.page]
    val size = request[PaginationParameters.size]
    // ...
}
```

## Parameter Validation Flow

When a request is processed, parameters go through the following validation flow:

1. **Extraction**: Parameters are extracted from the request
   - Path parameters from URL path segments
   - Query parameters from the query string
   - Header parameters from request headers

2. **Presence Check**: For required parameters, verify the parameter is present

3. **Empty Value Handling**: Apply `emptyAsMissing` logic if configured

4. **Type Validation and Conversion**: Apply the parameter's validator
   - Validates the string value
   - Converts to the target type
   - May throw `InvalidParameterException` if validation fails

5. **Invalid Value Handling**: Apply `invalidAsMissing` logic if configured and validation failed

6. **Condition Check**: Apply additional conditions if defined
   - Evaluated on the converted value
   - May return failure with a custom error message

7. **Default Value Application**: For optional parameters with defaults, apply the default value if the parameter is missing

8. **Availability**: Make validated parameters available to the handler

If any step fails, the framework generates an appropriate error response, typically a 400 Bad Request with details about the invalid parameter.

## Body Parameter Handling

Body parameters are handled differently from other parameter types:

1. The request body is read and parsed according to the Content-Type
2. The parsed data is converted to the target type
3. The typed body is made available in handlers via the `body` property

Body parameters are defined in route declarations:

```kotlin
POST("users") with body<CreateUserRequest>() isHandledBy {
    // Access body properties directly
    val name = body.name
    val email = body.email
    // ...
}
```

For more control over body parsing and validation, use the `parsing` handler:

```kotlin
val createUser by parsing<CreateUserRequest>() handling {
    // Additional validation
    if (body.name.isBlank()) {
        return@handling "Name cannot be blank".badRequest()
    }
    
    // Process the request
    userService.createUser(body.name, body.email)
    "User created".created
}
```

## Combining Parameters with Path Building

Snitch's path building syntax integrates seamlessly with parameters:

```kotlin
// Basic path with parameter
GET("users" / userId) isHandledBy { ... }

// Nested paths with multiple parameters
val api = routes {
    "users" / {
        GET() isHandledBy { ... }  // GET /users
        
        userId / {
            GET() isHandledBy { ... }  // GET /users/{userId}
            
            "posts" / {
                GET() isHandledBy { ... }  // GET /users/{userId}/posts
                
                postId / {
                    GET() isHandledBy { ... }  // GET /users/{userId}/posts/{postId}
                }
            }
        }
    }
}
```

This composition approach makes it easy to build hierarchical APIs with clear parameter scoping.

## Type-Safe Parameter Access

Snitch provides type-safe access to parameters in handlers:

```kotlin
val userId by path(ofInt)
val page by optionalQuery(ofInt, default = 1)

val handler by handling {
    val id: Int = request[userId]  // Type-safe access
    val pageNum: Int = request[page]  // Default applied if missing
    
    // ...
}
```

The type information is preserved through the property delegate system, so you get compile-time type checking for parameter access.

## Error Handling and Customization

You can customize how parameter validation errors are handled:

```kotlin
// Global error handler for parameter validation errors
app.handleException(InvalidParameterException::class) { exception, _ ->
    ErrorResponse(
        code = 400,
        message = "Invalid parameter: ${exception.message}"
    ).badRequest()
}

// Custom error handler for specific routes
routes {
    handleException(InvalidParameterException::class) { exception, _ ->
        ErrorResponse(
            code = 400,
            message = "Validation failed: ${exception.message}",
            details = mapOf("parameter" to exception.parameterName)
        ).badRequest()
    }
    
    // Routes with custom error handling...
}
```

## Working with Collections of Parameters

Handling collections of parameter values:

```kotlin
// Define a multi-value query parameter
val tags by query(description = "Filter by tags", multiValued = true)

// Access as a list in the handler
val handler by handling {
    val tagList: List<String> = request[tags]
    // ...
}
```

For more complex parsing:

```kotlin
// Define a collection parameter with custom parsing
val sortFields by query(
    validator { input ->
        input.split(",").map { field ->
            val parts = field.split(":")
            val name = parts[0]
            val direction = if (parts.size > 1) parts[1].uppercase() else "ASC"
            SortField(name, SortDirection.valueOf(direction))
        }
    },
    description = "Sorting fields in format field:direction,field:direction"
)

// Access in handler
val handler by handling {
    val sorting: List<SortField> = request[sortFields]
    // ...
}
```

## Performance Considerations

The parameter handling system is designed to be efficient:

1. **Property Delegates**: Parameters are defined once at initialization time
2. **Lazy Validation**: Parameters are only validated when accessed
3. **Caching**: Validation results are cached within a request
4. **Early Validation**: Basic validation happens before handlers are invoked, preventing unnecessary work

For optimal performance:

- Use appropriate validators for your parameter types
- Consider organizing parameters by usage patterns
- Leverage built-in validators when possible
- Use conditional logic in handlers for complex validation scenarios

## Integration with API Documentation

Parameters are automatically included in API documentation:

- Path parameters are shown in URL templates
- Query parameters are listed with types and descriptions
- Header parameters are included in expected headers
- Body parameters are documented with schemas when possible

To improve documentation:

- Add clear descriptions to all parameters
- Use appropriate visibility settings
- Organize parameters logically
- Consider using custom validators with descriptive error messages

## Best Practices

### Parameter Naming

Follow consistent naming conventions:

- Use camelCase for parameter names
- Be descriptive but concise
- Use singular nouns for single values (e.g., `userId`)
- Use plural nouns for collections (e.g., `tags`)
- Prefer specific names over generic ones (e.g., `email` instead of `value`)

### Parameter Organization

Organize parameters for maintainability:

- Group related parameters in objects
- Use common objects for shared parameters (e.g., pagination)
- Keep parameter definitions close to their usage
- Consider organizing parameters by domain concept

### Validation Strategy

Create a robust validation strategy:

- Validate parameters at the appropriate level
- Use parameter validators for basic type and format validation
- Use conditions for business rule validation
- Use handler logic for complex or cross-parameter validation
- Consider using the Jakarta Bean Validation API for complex objects

### Parameter Documentation

Document parameters thoroughly:

- Add clear descriptions to all parameters
- Indicate parameter constraints (e.g., "must be positive")
- Document default values for optional parameters
- Use examples for complex parameters

### Parameter Security

Consider security implications:

- Validate all user input
- Sanitize path and query parameters
- Be cautious with header parameters that may contain sensitive data
- Consider using `emptyAsMissing` for parameters that should never be empty
- Use conditions to implement additional security checks

## Conclusion

Snitch's parameter handling system provides a powerful, type-safe approach to working with request parameters. By leveraging Kotlin's property delegation feature, it offers a declarative syntax for parameter definition with strong type safety and robust validation.

Understanding the internals of the parameter system allows you to build more maintainable, secure, and well-documented APIs with Snitch, while taking advantage of the framework's type safety and validation features.