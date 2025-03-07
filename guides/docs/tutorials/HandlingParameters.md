# Handling Parameters in Snitch

This tutorial covers how to define, access, and validate different types of parameters in Snitch applications. Parameters are a crucial part of building APIs as they allow your endpoints to receive and process various kinds of input data.

## Parameter Types

Snitch supports several types of parameters:

1. **Path Parameters**: Variables embedded in the URL path
2. **Query Parameters**: Key-value pairs in the URL query string
3. **Header Parameters**: HTTP request headers
4. **Body Parameters**: Data in the request body (typically JSON)

Let's explore how to use each type of parameter in your Snitch applications.

## Defining Parameters

Parameters in Snitch are defined using property delegates, making them type-safe and easy to use. 

### Path Parameters

Path parameters are defined using the `path()` delegate and are embedded directly in the URL path:

```kotlin
// Define a path parameter
val userId by path(ofInt, description = "User ID")

// Use it in a route
GET("users" / userId) isHandledBy { 
    // Handler code
}
```

The `ofInt` parameter is a validator that ensures the parameter can be parsed as an integer.

### Query Parameters

Query parameters are defined using the `query()` delegate:

```kotlin
// Define a query parameter
val page by query(ofInt, description = "Page number")
val limit by optionalQuery(ofInt, default = 10, description = "Items per page")

// Use it in a route
GET("users") withQuery page isHandledBy {
    // Handler code
}
```

### Header Parameters

Header parameters are defined using the `header()` delegate:

```kotlin
// Define a header parameter
val authorization by header(description = "Authorization token")

// Use it in a route
GET("secured") withHeader authorization isHandledBy {
    // Handler code
}
```

### Body Parameters

Body parameters are defined directly in the route definition:

```kotlin
// Define a route with a body parameter
POST("users") with body<CreateUserRequest>() isHandledBy {
    // Handler code
}
```

## Accessing Parameters in Handlers

Once parameters are defined, you can access them in your handlers using the request object.

### Path Parameters

Access path parameters using indexed syntax:

```kotlin
val userId by path(ofInt)

val getUser by handling {
    val id = request[userId]
    userService.getUser(id)?.ok ?: "User not found".notFound()
}
```

### Query Parameters

Query parameters are accessed the same way:

```kotlin
val page by query(ofInt)
val limit by optionalQuery(ofInt, default = 10)

val listUsers by handling {
    val pageNum = request[page]
    val pageSize = request[limit]
    userService.getUsers(pageNum, pageSize).ok
}
```

### Header Parameters

Headers are accessed using the same syntax:

```kotlin
val authorization by header()

val securedEndpoint by handling {
    val authHeader = request[authorization]
    if (isValidToken(authHeader)) {
        "Authenticated".ok
    } else {
        "Unauthorized".unauthorized()
    }
}
```

### Body Parameters

Body parameters are accessed using the `body` property:

```kotlin
val createUser by parsing<CreateUserRequest>() handling {
    val name = body.name
    val email = body.email
    userService.createUser(name, email)
    "User created".created
}
```

## Optional vs Required Parameters

By default, parameters defined with `path()`, `query()`, and `header()` are required. If a required parameter is missing or invalid, Snitch automatically returns a 400 Bad Request response.

For optional parameters, use the `optionalPath()`, `optionalQuery()`, and `optionalHeader()` delegates:

```kotlin
// Required parameter
val userId by path(ofInt)

// Optional parameter with default value
val page by optionalQuery(ofInt, default = 1)

// Optional parameter without default (will be null if missing)
val filter by optionalQuery()
```

When accessing optional parameters without defaults, check for null values:

```kotlin
val listUsers by handling {
    val filterValue = request[filter]
    val users = if (filterValue != null) {
        userService.getUsersWithFilter(filterValue)
    } else {
        userService.getAllUsers()
    }
    users.ok
}
```

## Parameter Validation

Snitch provides built-in validators for common parameter types:

- `ofInt`: Validates that the parameter is an integer
- `ofLong`: Validates that the parameter is a long integer
- `ofDouble`: Validates that the parameter is a double
- `ofBoolean`: Validates that the parameter is a boolean
- `ofNonNegativeInt`: Validates that the parameter is a non-negative integer
- `ofEmail`: Validates that the parameter is a valid email address
- `matches(regex)`: Validates that the parameter matches a regular expression

You can also create custom validators:

```kotlin
// Custom validator for UUIDs
val ofUUID = validator { input ->
    try {
        UUID.fromString(input)
        input
    } catch (e: IllegalArgumentException) {
        throw InvalidParameterException("Invalid UUID format")
    }
}

// Use the custom validator
val orderId by path(ofUUID, description = "Order ID in UUID format")
```

## Real-World Example

Here's a more complete example showing different parameter types in action:

```kotlin
// Parameter definitions
val userId by path(ofInt, description = "User ID")
val page by optionalQuery(ofNonNegativeInt, default = 1, description = "Page number")
val limit by optionalQuery(ofNonNegativeInt, default = 10, description = "Items per page")
val authorization by header(description = "Bearer token")

// Route definitions
val routes = routes {
    // Get user by ID
    GET("users" / userId) isHandledBy {
        val id = request[userId]
        userService.getUser(id)?.ok ?: "User not found".notFound()
    }
    
    // List users with pagination
    GET("users") withQuery page withQuery limit isHandledBy {
        val pageNum = request[page]
        val pageSize = request[limit]
        userService.getUsers(pageNum, pageSize).ok
    }
    
    // Create user with request body
    POST("users") with body<CreateUserRequest>() isHandledBy {
        val newUser = userService.createUser(body.name, body.email)
        newUser.created
    }
    
    // Secured endpoint with authorization header
    GET("secured") withHeader authorization isHandledBy {
        val token = request[authorization]
        if (isValidToken(token)) {
            "Authenticated".ok
        } else {
            "Unauthorized".unauthorized()
        }
    }
}
```

## Advanced Parameter Handling

### Organizing Parameters

For better organization, you can group related parameters in objects:

```kotlin
object Paths {
    val userId by path(ofInt)
    val postId by path(ofInt)
}

object Queries {
    val page by optionalQuery(ofNonNegativeInt, default = 1)
    val limit by optionalQuery(ofNonNegativeInt, default = 10)
}

object Headers {
    val authorization by header()
}

// Using organized parameters
GET("users" / Paths.userId / "posts" / Paths.postId) isHandledBy {
    val userId = request[Paths.userId]
    val postId = request[Paths.postId]
    // ...
}
```

### Parameter Conditions

You can add conditions to parameters that must be satisfied:

```kotlin
// Define a condition for token validation
val validToken = condition<String> { token ->
    jwtService.validateToken(token)
}

// Apply the condition to a parameter
val accessToken by header(
    condition = validToken,
    name = "X-Access-Token",
    description = "Valid access token"
)
```

### Custom Parameter Processing

For complex parameter processing, you can use the `parsing` handler:

```kotlin
val createUser by parsing<CreateUserRequest>() handling {
    // Validate and transform the request body
    val sanitizedName = sanitizeInput(body.name)
    val normalizedEmail = normalizeEmail(body.email)
    
    // Use the processed parameters
    userService.createUser(sanitizedName, normalizedEmail)
    "User created".created
}
```

## Best Practices

1. **Use Descriptive Names**: Choose parameter names that clearly indicate their purpose.

2. **Add Descriptions**: Include descriptions for all parameters to improve API documentation.

3. **Validate Inputs**: Use appropriate validators to ensure parameters meet your requirements.

4. **Handle Errors Gracefully**: Provide meaningful error messages when parameter validation fails.

5. **Group Related Parameters**: Organize parameters into logical groups for better code organization.

6. **Use Appropriate Parameter Types**: Choose the right parameter type (path, query, header, body) based on your API design.

7. **Make Parameters Optional When Appropriate**: Don't require parameters that aren't strictly necessary.

8. **Set Sensible Defaults**: Provide meaningful default values for optional parameters.

## Conclusion

Snitch's parameter handling system provides a type-safe, declarative way to define and validate request parameters. By using the right parameter types and validation rules, you can create robust APIs that gracefully handle various input scenarios.