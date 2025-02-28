# Mastering Snitch Parameters

Parameters are a fundamental part of HTTP communication, allowing clients to send data to your server. Snitch provides an expressive, type-safe approach to handling various types of parameters including path parameters, query parameters, headers, and request bodies.

This tutorial will guide you through the complete parameter system in Snitch, from basic usage to advanced validation and transformation techniques.

## Basic Parameter Types

Snitch supports all common HTTP parameter types:

- **Path parameters**: Values embedded in the URL path (e.g., `/users/{userId}`)
- **Query parameters**: Key-value pairs appended to the URL (e.g., `?page=1&limit=10`)
- **Header parameters**: HTTP headers sent with the request
- **Request body**: Data sent in the request payload

Let's see how each is defined and accessed.

## Path Parameters

Path parameters are defined using the `path()` factory function:

```kotlin
// Define a path parameter
val userId by path()
```

Once defined, you can use it in route definitions:

```kotlin
GET("users" / userId) isHandledBy {
    // Access the parameter value with request[userId]
    val userIdValue = request[userId]
    userRepository.findById(userIdValue).ok
}
```

You can also define paths with validation in one line:

```kotlin
// Define a path parameter with validation
val postId by path(ofNonEmptyString, description = "Post identifier")
```

## Query Parameters

Query parameters are defined using the `query()` factory function:

```kotlin
// Define a required query parameter
val searchTerm by query(description = "Term to search for")

// With validation
val limit by query(ofNonNegativeInt, description = "Maximum number of results")
```

Using query parameters in routes:

```kotlin
GET("search") withQuery searchTerm withQuery limit isHandledBy {
    // Access the parameters
    val term = request[searchTerm] // String
    val maxResults = request[limit] // Int
    
    searchService.search(term, maxResults).ok
}
```

## Header Parameters

Header parameters are defined using the `header()` factory function:

```kotlin
// Define a required header parameter
val contentType by header(description = "Content type of the request")

// With validation
val apiVersion by header(ofNonEmptyString, description = "API version to use")
```

Using header parameters:

```kotlin
POST("data") withHeader contentType withHeader apiVersion isHandledBy {
    // Access the headers
    val type = request[contentType]
    val version = request[apiVersion]
    
    // Use the header values
    dataService.processData(request.body(), type, version).created
}
```

## Request Body

Request bodies are handled differently from other parameters. Instead of defining them separately, you declare them directly in the route definition:

```kotlin
// Define a route with a body parameter
POST("users") with body<CreateUserRequest>() isHandledBy {
    // Access the body with the `body` property
    val newUser = body
    
    userService.createUser(newUser.name, newUser.email).created
}
```

For more complex scenarios, you can use the `parsing` function:

```kotlin
val createUser by parsing<CreateUserRequest>() handling {
    userService.createUser(body.name, body.email).created
}

// Use the handler
POST("users") with body<CreateUserRequest>() isHandledBy createUser
```

## Parameter Validation and Transformation

Snitch parameters are not just for accessing raw values - they also validate and transform the input data. The framework provides several built-in validators:

```kotlin
// String validation
val name by query(ofNonEmptyString)

// Numeric validation
val age by query(ofPositiveInt)
val price by query(ofNonNegativeDouble)

// Boolean validation
val enabled by query(ofBoolean)

// Date validation
val birthdate by query(ofIsoDate)
```

These validators:
1. Check if the input matches expected format
2. Convert the input to the appropriate type
3. Return 400 Bad Request with descriptive error messages if validation fails

## Optional Parameters

Not all parameters are required. For optional parameters, use the `optionalQuery()` and `optionalHeader()` functions:

```kotlin
// Optional parameter without default (can be null)
val sort by optionalQuery(ofNonEmptyString, description = "Sort direction")

// Optional parameter with default value
val page by optionalQuery(ofNonNegativeInt, default = 1, description = "Page number")
val limit by optionalQuery(ofNonNegativeInt, default = 20, description = "Results per page")
```

When using optional parameters:

```kotlin
GET("users") withQuery page withQuery limit withQuery sort isHandledBy {
    // Access the parameters (with default values applied)
    val pageNum = request[page]     // Int, defaults to 1 if not provided
    val pageSize = request[limit]   // Int, defaults to 20 if not provided
    val sortDir = request[sort]     // String or null if not provided
    
    userService.getUsers(pageNum, pageSize, sortDir).ok
}
```

## Custom Validators

While built-in validators cover many scenarios, you'll often need custom validation logic, especially when working with domain-specific types.

### Creating Custom Validators

Use `stringValidator` or `validator` to create custom validators:

```kotlin
// Simple custom validator for email format
val ofEmail = stringValidator("valid email address", """^[\w.%+-]+@[\w.-]+\.[a-zA-Z]{2,}$""".toRegex()) { it }

// Custom validator with transformation to domain type
val ofUserId = stringValidator("valid UUID") { UserId(UUID.fromString(it)) }
```

Using custom validators:

```kotlin
val email by query(ofEmail, description = "User email address")
val userId by path(ofUserId, description = "User identifier")
```

## Domain Type Conversion

Converting raw parameter values to domain types is a best practice. Snitch makes this seamless:

```kotlin
// Define domain types
data class UserId(val value: UUID)
data class OrderStatus(val value: String) {
    init {
        require(value in listOf("PENDING", "COMPLETED", "CANCELLED")) {
            "Invalid order status: $value"
        }
    }
}

// Create validators for domain types
val ofUserId = stringValidator("valid user ID") { UserId(UUID.fromString(it)) }
val ofOrderStatus = stringValidator("order status") { OrderStatus(it.uppercase()) }

// Use with parameters
val userId by path(ofUserId)
val status by query(ofOrderStatus)
```

## Parameter Naming

By default, parameter names in requests match their property names in code. You can customize this:

```kotlin
// Custom parameter name
val searchQuery by query(name = "q", description = "Search query")
val userIdentifier by path(name = "user_id", description = "User ID")
```

With these definitions:
- The query parameter will be accessed as `?q=search terms`
- The path parameter will be defined as `/users/{user_id}`

## Advanced Parameter Handling

### Handling Empty Values

Control how empty values are treated:

```kotlin
// Empty values treated as missing
val tag by query(emptyAsMissing = true)

// Invalid values treated as missing (for optional parameters)
val count by optionalQuery(ofNonNegativeInt, invalidAsMissing = true, default = 0)
```

### Parameter Visibility in Documentation

Control whether parameters appear in public API documentation:

```kotlin
// Internal parameter not shown in public docs
val debugMode by optionalQuery(ofBoolean, visibility = Visibility.INTERNAL)
```

### Handling Multiple Values

For parameters that may be provided multiple times:

```kotlin
// Define a repeatable parameter
val tags by query(ofStringSet)  // Will collect all values into a Set<String>

// Custom repeatable validator
val ofUserIds = repeatableValidator<String, UserId> { UserId(UUID.fromString(it)) }
val userIds by query(ofUserIds)  // Will collect all values into a Collection<UserId>
```

## Best Practices

1. **Use Domain Types**: Convert raw strings to meaningful domain types as early as possible.

2. **Validate Thoroughly**: Define validation rules that catch all potential issues before they reach your business logic.

3. **Provide Descriptive Error Messages**: Set clear validator descriptions so clients receive helpful error messages.

4. **Set Sensible Defaults**: For optional parameters, choose default values that make sense for most use cases.

5. **Document Parameters**: Always include a description for parameters to generate comprehensive API documentation.

6. **Consistent Naming**: Use a consistent naming convention for parameter properties.

7. **Explicitly Register Parameters**: Always declare parameter usage with `withQuery`, `withHeader`, etc., even if the validation is done elsewhere.

```kotlin
// Good practice
GET("users") withQuery page withQuery limit isHandledBy { ... }

// Not recommended (parameters not explicitly registered)
GET("users") isHandledBy {
    // Using parameters without registering them
    val pageValue = request.queryParams("page")
}
```

## Complete Example

Here's a complete example demonstrating different parameter types together:

```kotlin
// Domain types
data class UserId(val value: UUID)
data class PostId(val value: String)

// Validators
val ofUserId = stringValidator("valid user ID") { UserId(UUID.fromString(it)) }
val ofPostId = stringValidator("valid post ID") { PostId(it) }

// Parameters
val userId by path(ofUserId, description = "User identifier")
val postId by path(ofPostId, description = "Post identifier")
val includeComments by query(ofBoolean, description = "Whether to include comments")
val limit by optionalQuery(ofNonNegativeInt, default = 10, description = "Maximum results")
val apiVersion by header(ofNonEmptyString, description = "API version")

// Routes
val userController = routes {
    GET("users" / userId) withHeader apiVersion isHandledBy getUser
    
    GET("users" / userId / "posts") withQuery limit isHandledBy getUserPosts
    
    GET("posts" / postId) withQuery includeComments isHandledBy getPost
}

// Handlers
val getUser by handling {
    val user = userService.getUser(request[userId])
    user.ok
}

val getUserPosts by handling {
    val posts = postService.getUserPosts(
        userId = request[userId],
        limit = request[limit]
    )
    posts.ok
}

val getPost by handling {
    val post = postService.getPost(
        postId = request[postId],
        includeComments = request[includeComments]
    )
    post.ok
}
```

With Snitch's parameter system, you can build robust, type-safe APIs that validate input data before it reaches your business logic, resulting in cleaner code and better error handling.