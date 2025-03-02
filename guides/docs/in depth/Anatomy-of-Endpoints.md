# Endpoint DSL

Endpoints are the fundamental building blocks of your API in Snitch. This guide explores the internal structure of the Endpoint DSL, explaining how endpoints are defined, configured, and composed to create expressive, type-safe APIs.

## The Endpoint Data Class

At the core of Snitch's routing system is the `Endpoint` data class:

```kotlin
data class Endpoint<T: Any>(
    val method: Method,
    val path: String,
    val parameters: List<Parameter<*, *>> = emptyList(),
    val conditions: List<Condition> = emptyList(),
    val decorations: List<Decoration> = emptyList(),
    val beforeActions: List<RequestWrapper.() -> Response?> = emptyList(),
    val afterActions: List<RequestWrapper.() -> Unit> = emptyList(),
    val handler: (RequestWrapper.() -> T)? = null
)
```

Let's examine each component:

1. **Type Parameter**:
   - `T`: The return type of the handler function, which determines the response type

2. **Properties**:
   - `method`: The HTTP method (GET, POST, etc.)
   - `path`: The URL path, potentially including parameter placeholders
   - `parameters`: List of parameters (path, query, header, body) this endpoint uses
   - `conditions`: List of conditions that must be satisfied for the endpoint to execute
   - `decorations`: List of decorations that modify the endpoint's behavior
   - `beforeActions`: Actions executed before the handler runs
   - `afterActions`: Actions executed after the handler completes
   - `handler`: The function that processes the request and produces a response

The data class design is crucial for Snitch's flexibility and composability. Since endpoints are immutable data objects, they can be transformed and combined in powerful ways without side effects.

## Creating Endpoints

Endpoints are typically created through the HTTP method functions and then configured with additional features.

### HTTP Method Functions

Snitch provides functions for each HTTP method:

```kotlin
fun GET(path: String = ""): Endpoint<Any> = 
    Endpoint(method = Method.GET, path = ensureLeadingSlash(path))

fun POST(path: String = ""): Endpoint<Any> = 
    Endpoint(method = Method.POST, path = ensureLeadingSlash(path))

fun PUT(path: String = ""): Endpoint<Any> = 
    Endpoint(method = Method.PUT, path = ensureLeadingSlash(path))

fun DELETE(path: String = ""): Endpoint<Any> = 
    Endpoint(method = Method.DELETE, path = ensureLeadingSlash(path))

fun PATCH(path: String = ""): Endpoint<Any> = 
    Endpoint(method = Method.PATCH, path = ensureLeadingSlash(path))

fun OPTIONS(path: String = ""): Endpoint<Any> = 
    Endpoint(method = Method.OPTIONS, path = ensureLeadingSlash(path))

fun HEAD(path: String = ""): Endpoint<Any> = 
    Endpoint(method = Method.HEAD, path = ensureLeadingSlash(path))
```

Each function creates an `Endpoint` with the specified HTTP method and path, returning a fresh `Endpoint` instance ready for further configuration.

**Usage Example**:

```kotlin
GET("users")       // Creates a GET endpoint for /users
POST("users")      // Creates a POST endpoint for /users
PUT("users/123")   // Creates a PUT endpoint for /users/123
```

### Path Construction

Paths can be constructed in several ways:

1. **String literals**:
   ```kotlin
   GET("users/profile")
   ```

2. **Path parameters**:
   ```kotlin
   val userId by path()
   GET("users" / userId)
   ```

3. **Path composition with the `/` operator**:
   ```kotlin
   GET("users" / userId / "posts" / postId)
   ```

The `/` operator is an extension function on `String` that concatenates path segments:

```kotlin
operator fun String.div(other: String): String = 
    "$this/$other".replace("//", "/")

operator fun String.div(param: Parameter<*, *>): String = 
    "$this/{${param.name}}".replace("//", "/")
```

This elegant approach allows paths to be constructed in a readable, composable way.

### Route Nesting

Snitch supports route nesting through a DSL that allows hierarchical organization:

```kotlin
routes {
    "api" / {
        "v1" / {
            "users" / {
                GET() isHandledBy getUsersHandler
                POST() with body<CreateUserRequest>() isHandledBy createUserHandler
                
                userId / {
                    GET() isHandledBy getUserHandler
                    PUT() with body<UpdateUserRequest>() isHandledBy updateUserHandler
                    DELETE() isHandledBy deleteUserHandler
                }
            }
        }
    }
}
```

Behind the scenes, this is implemented using a hierarchical context that tracks the current path prefix:

```kotlin
class RouterContext(private val pathPrefix: String = "") {
    fun String.div(block: RouterContext.() -> Unit) {
        val newContext = RouterContext("$pathPrefix/$this".replace("//", "/"))
        newContext.block()
    }
    
    fun GET(path: String = ""): Endpoint<Any> = 
        Endpoint(Method.GET, "$pathPrefix/$path".replace("//", "/"))
    
    // Other HTTP method functions...
}
```

This approach allows you to organize routes according to your API's logical structure.

## Configuring Endpoints

Once an endpoint is created, it can be configured with various features. These configurations are applied through extension functions that return new `Endpoint` instances with the desired modifications.

### Parameters

Parameters are added using the `with` function and its variants:

```kotlin
fun <T: Any> Endpoint<T>.with(vararg params: Parameter<*, *>): Endpoint<T> =
    copy(parameters = parameters + params)

fun <T: Any> Endpoint<T>.withQueries(vararg params: Parameter<*, *>): Endpoint<T> =
    with(*params)

fun <T: Any> Endpoint<T>.withHeaders(vararg params: Parameter<*, *>): Endpoint<T> =
    with(*params)
```

**Usage Example**:

```kotlin
val limit by query(ofIntRange(1, 100), default = 20)
val offset by query(ofNonNegativeInt, default = 0)
val apiKey by header(ofNonEmptyString)

GET("users")
    .withQueries(limit, offset)
    .withHeaders(apiKey)
```

Internally, these functions simply add the parameters to the endpoint's parameter list, making them available for validation and access in the handler.

### Conditions

Conditions are added using the `onlyIf` function:

```kotlin
infix fun <T: Any> Endpoint<T>.onlyIf(condition: Condition): Endpoint<T> =
    copy(conditions = conditions + condition)
```

**Usage Example**:

```kotlin
val hasAdminRole = condition("hasAdminRole") { /* implementation */ }

GET("admin/dashboard") onlyIf hasAdminRole
```

The `onlyIf` function appends the condition to the endpoint's conditions list. During request processing, all conditions are evaluated before the handler executes.

### Decorations

Decorations are added using the `decorated` function:

```kotlin
infix fun <T: Any> Endpoint<T>.decorated(with: Decoration): Endpoint<T> =
    copy(decorations = decorations + with)
```

**Usage Example**:

```kotlin
val withLogging = decoration { /* implementation */ }

GET("users") decorated withLogging
```

Decorations provide a way to wrap handler execution with custom logic, similar to middleware in other frameworks.

### Before and After Actions

Before and after actions allow executing code before and after the handler:

```kotlin
fun <T: Any> Endpoint<T>.doBefore(action: RequestWrapper.() -> Response?): Endpoint<T> =
    copy(beforeActions = beforeActions + action)

fun <T: Any> Endpoint<T>.doAfter(action: RequestWrapper.() -> Unit): Endpoint<T> =
    copy(afterActions = afterActions + action)
```

**Usage Example**:

```kotlin
GET("users")
    .doBefore { 
        logger.info("Accessing users endpoint")
        // Optionally return a Response to short-circuit
        null 
    }
    .doAfter { 
        logger.info("Completed users endpoint request")
    }
```

These functions append actions to the respective lists in the endpoint. During request processing, before actions run in reverse declaration order (last declared, first executed), while after actions run in declaration order.

## Endpoint Handlers

While we won't delve deeply into handlers here, it's worth understanding how they connect to endpoints:

```kotlin
infix fun <T: Any> Endpoint<T>.isHandledBy(handler: RequestWrapper.() -> T): Endpoint<T> =
    copy(handler = handler)
```

The `isHandledBy` function associates a handler with an endpoint. The handler is a function that:
1. Receives a `RequestWrapper` as its receiver
2. Returns a value of type `T`, which determines the response type

This type-safe design ensures that handlers return appropriate values that can be converted to HTTP responses.

## The Router Interface

The `Router` interface defines a collection of endpoints:

```kotlin
interface Router {
    val endpoints: List<Endpoint<*>>
}
```

Routers can be composed and nested, allowing for modular API organization:

```kotlin
fun routes(block: RouterBuilder.() -> Unit): Router {
    val builder = RouterBuilder()
    builder.block()
    return builder.build()
}
```

The `RouterBuilder` class accumulates endpoints during DSL execution:

```kotlin
class RouterBuilder {
    private val mutableEndpoints = mutableListOf<Endpoint<*>>()
    
    fun <T: Any> endpoint(endpoint: Endpoint<T>) {
        mutableEndpoints.add(endpoint)
    }
    
    fun build(): Router = object : Router {
        override val endpoints = mutableEndpoints.toList()
    }
}
```

This builder-based approach allows for a clean DSL while maintaining immutability of the resulting routers.

## Extension and Customization

One of Snitch's most powerful features is its extensibility. Since endpoints are data classes and the DSL is built from extension functions, you can easily add new capabilities.

### Extending Endpoint with New Capabilities

You can add new features to endpoints by defining extension functions:

```kotlin
fun <T: Any> Endpoint<T>.withTimeout(milliseconds: Long): Endpoint<T> =
    decorated(TimeoutDecoration(milliseconds))

// Usage
GET("slow-operation") withTimeout 5000
```

This approach allows you to create domain-specific extensions tailored to your application's needs.

### Creating DSL Extensions

You can even extend the DSL with new constructs:

```kotlin
infix fun <T: Any> Endpoint<T>.v(version: Int): Endpoint<T> =
    copy(path = path.replace("/v1/", "/v$version/"))

// Usage
GET("v1/users") v 2 isHandledBy getUsersV2Handler
```

This creates an expressive way to define versioned endpoints. Because endpoints are data classes, transformations like this are straightforward and composable.

## Type Safety Aspects

Snitch's Endpoint DSL is designed with type safety as a primary consideration:

1. **Parameter Type Safety**:
   ```kotlin
   val userId by path(ofLong)
   
   // In the handler:
   val id: Long = request[userId] // Type-safe access
   ```

2. **Handler Return Types**:
   ```kotlin
   GET("users") isHandledBy {
       // Must return a value compatible with the endpoint type
       listOf("user1", "user2").ok
   }
   ```

3. **Condition Composition**:
   ```kotlin
   GET("resource") onlyIf (isAuthenticated and (isResourceOwner or hasAdminRole))
   ```
   The boolean operators (`and`, `or`, `not`) are type-checked at compile time.

4. **Method Chaining**:
   ```kotlin
   GET("users")
       .withQueries(limit, offset)
       .onlyIf(isAuthenticated)
       .doBefore { /* ... */ }
       .isHandledBy { /* ... */ }
   ```
   Each method returns the appropriate endpoint type, ensuring the chain remains type-safe.

This comprehensive type safety catches many potential errors at compile time, dramatically reducing runtime issues.

## Under the Hood: Request Processing

When a request arrives, Snitch processes it through several stages:

1. **Route Matching**: Snitch finds the endpoint that matches the HTTP method and path
2. **Parameter Extraction and Validation**: Parameters are extracted from the request and validated
3. **Condition Evaluation**: All conditions are evaluated; if any fail, the request is rejected
4. **Decoration Setup**: Decorations are arranged to wrap the handler execution
5. **Before Actions**: Before actions are executed in reverse order
6. **Handler Execution**: The handler processes the request
7. **After Actions**: After actions are executed in declaration order

This pipeline is reflected in the `Endpoint` data class structure, with each component corresponding to a stage in request processing.

## Best Practices

Based on the internal workings of endpoints, here are some best practices:

1. **Organize by Resource**: Structure your routes around resources and sub-resources
   ```kotlin
   "users" / {
       GET() // List users
       POST() // Create user
       
       userId / {
           GET() // Get user
           PUT() // Update user
           DELETE() // Delete user
           
           "posts" / {
               // User's posts resources
           }
       }
   }
   ```

2. **Keep Endpoints Focused**: Each endpoint should handle a single responsibility

3. **Extract Shared Logic**: Use decorations and conditions to extract cross-cutting concerns
   ```kotlin
   val authenticated = decorateWith { /* authentication logic */ }
   
   authenticated {
       // All routes here require authentication
   }
   ```

4. **Leverage Type-Safe Parameters**: Define all parameters with appropriate validators
   ```kotlin
   val limit by query(ofIntRange(1, 100), default = 20)
   
   // Better than:
   val limit by query() // String that needs manual validation
   ```

5. **Use Extension Methods for Common Patterns**: Create extension functions for frequent use cases
   ```kotlin
   fun <T: Any> Endpoint<T>.withCache(durationSeconds: Int): Endpoint<T> =
       decorated(CacheDecoration(durationSeconds))
   ```

6. **Modularize Routers**: Break large APIs into smaller, composable routers
   ```kotlin
   val userRoutes = routes { /* user endpoints */ }
   val postRoutes = routes { /* post endpoints */ }
   
   val apiRoutes = routes {
       "api" / {
           "users" / userRoutes
           "posts" / postRoutes
       }
   }
   ```

7. **Follow RESTful Conventions**: Use appropriate HTTP methods for different operations
   ```kotlin
   GET(resourceId)   // Read a resource
   POST()            // Create a resource
   PUT(resourceId)   // Update a resource
   DELETE(resourceId) // Delete a resource
   ```

## Conclusion

The Endpoint DSL in Snitch provides a powerful, type-safe way to define and configure API endpoints. By understanding its internal structure and capabilities, you can create expressive, maintainable APIs that leverage Kotlin's type system for robust error checking.

The data class foundation, combined with extension functions and builders, creates a DSL that is both flexible and type-safe, allowing for easy customization while catching errors at compile time.

This design exemplifies how thoughtful API design can leverage language features to create expressive yet safe interfaces for complex functionality.