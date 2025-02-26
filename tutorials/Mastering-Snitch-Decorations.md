# Mastering Snitch Decorations

Decorations are a powerful feature in Snitch that allow you to modify request handling behavior across multiple routes. They provide a clean, composable way to implement cross-cutting concerns like logging, authentication, transaction management, and more. This tutorial will guide you through everything you need to know about decorations, from basic usage to advanced patterns.

## Table of Contents

- [Understanding Decorations](#understanding-decorations)
- [Basic Decoration Usage](#basic-decoration-usage)
- [Creating Custom Decorations](#creating-custom-decorations)
- [Decoration Composition](#decoration-composition)
- [Nesting Decorations](#nesting-decorations)
- [Decoration Order and Execution Flow](#decoration-order-and-execution-flow)
- [Integration with Conditions](#integration-with-conditions)
- [Common Use Cases](#common-use-cases)
- [Best Practices](#best-practices)
- [Real-World Examples](#real-world-examples)

## Understanding Decorations

In Snitch, a decoration is a higher-order function that wraps around route handlers to modify their behavior. Decorations can:

1. Execute code before the handler runs
2. Execute code after the handler runs
3. Transform the response from the handler
4. Short-circuit the request and return a response without calling the handler
5. Handle exceptions thrown by the handler

Decorations are implemented using the `decorateWith` function, which creates a decorator that can be applied to routes or route hierarchies.

## Basic Decoration Usage

The simplest way to use decorations is to apply them to a route or route hierarchy:

```kotlin
val logged = decorateWith {
    val method = method.name
    logger().info("Begin Request: $method $path")
    next().also {
        logger().info("End Request: $method $path ${it.statusCode.code} ${it.value(parser)}")
    }
}

val routes = routes {
    logged {
        GET("hello").isHandledBy { "Hello, world!".ok }
        POST("users").with(body<CreateUserRequest>()).isHandledBy { createUser() }
    }
}
```

In this example, all requests to `/hello` and `/users` will be logged before and after handling.

## Creating Custom Decorations

You can create custom decorations using the `decorateWith` function:

```kotlin
val withTransaction = decorateWith { 
    transaction { 
        next() 
    } 
}
```

The lambda passed to `decorateWith` receives a `RequestWrapper` implicitly and should call `next()` to execute the next decoration or handler in the chain. It should return a `Response`.

### Parameterized Decorations

You can create reusable decoration factories that accept parameters:

```kotlin
fun withMetricLabel(label: String) = decorateWith {
    val startTime = System.currentTimeMillis()
    val response = next()
    val endTime = System.currentTimeMillis()
    metrics.record(label, endTime - startTime)
    response
}

// Usage
withMetricLabel("user-service") {
    GET("users").isHandledBy { getUsers() }
}
```

## Decoration Composition

One of the most powerful features of Snitch decorations is their composability. You can combine multiple decorations using the `+` operator:

```kotlin
val combinedDecoration = logged + withTransaction

// Usage
combinedDecoration {
    GET("users").isHandledBy { getUsers() }
}
```

When decorations are composed, they are applied from right to left. In the example above, the execution order would be:

1. `withTransaction`
2. `logged`
3. The actual handler

This means that the request flows through the decorations in the order they are composed, and the response flows back in the reverse order.

### Composition Properties

Decoration composition has several important properties:

1. **Associativity**: `(a + b) + c` is equivalent to `a + (b + c)`
2. **Identity**: There exists an identity decoration that, when composed with any decoration, yields the original decoration
3. **Right-to-left evaluation**: In `a + b + c`, decoration `c` is applied first, then `b`, then `a`

These properties make decorations a powerful tool for building complex middleware chains.

## Nesting Decorations

In addition to composition, decorations can also be nested:

```kotlin
logged {
    withTransaction {
        GET("users").isHandledBy { getUsers() }
    }
}
```

When decorations are nested, they are applied from outside to inside. In the example above, the execution order would be:

1. `logged`
2. `withTransaction`
3. The actual handler

This is different from composition, where the order is right to left.

## Decoration Order and Execution Flow

Understanding the execution flow of decorations is crucial for using them effectively. Let's look at a more complex example:

```kotlin
// Composition
(decoration1 + decoration2) {
    // Nesting
    decoration3 {
        GET("hello").isHandledBy { "Hello, world!".ok }
    }
}
```

In this example, the execution order would be:

1. `decoration2` (from composition, right to left)
2. `decoration1` (from composition, right to left)
3. `decoration3` (from nesting, outside to inside)
4. The actual handler

And the response would flow back in the reverse order:

1. The actual handler
2. `decoration3`
3. `decoration1`
4. `decoration2`

This allows you to create sophisticated middleware chains with precise control over the execution order.

## Integration with Conditions

Decorations work seamlessly with Snitch's condition system. You can use the `transformEndpoints` function to create decorations that also add parameters and apply conditions:

```kotlin
val authenticated = transformEndpoints {
    with(listOf(accessToken)).decorated {
        when (request[accessToken]) {
            is Authentication.Authenticated -> next()
            is Authentication.Unauthenticated -> UNAUTHORIZED()
        }
    }
}

// Usage
authenticated {
    GET("profile").isHandledBy { getProfile() }
}
```

This approach is particularly useful for authentication and authorization, where you need to both add parameters (like access tokens) and check conditions before proceeding.

## Common Use Cases

### Logging

```kotlin
val logged = decorateWith {
    val method = method.name
    logger().info("Begin Request: $method $path")
    next().also {
        logger().info("End Request: $method $path ${it.statusCode.code} ${it.value(parser)}")
    }
}
```

### Authentication

```kotlin
val authenticated = transformEndpoints {
    with(listOf(accessToken)).decorated {
        when (request[accessToken]) {
            is Authentication.Authenticated -> next()
            is Authentication.Unauthenticated -> UNAUTHORIZED()
        }
    }
}
```

### Transaction Management

```kotlin
val withTransaction = decorateWith { 
    transaction { 
        next() 
    } 
}
```

### Error Handling

```kotlin
val handleErrors = decorateWith {
    try {
        next()
    } catch (e: Exception) {
        logger.error("Error handling request", e)
        "Internal server error".serverError()
    }
}
```

### Response Transformation

```kotlin
val addCorsHeaders = decorateWith {
    val response = next()
    response.copy(
        headers = response.headers + mapOf(
            "Access-Control-Allow-Origin" to "*",
            "Access-Control-Allow-Methods" to "GET, POST, PUT, DELETE, OPTIONS",
            "Access-Control-Allow-Headers" to "Content-Type, Authorization"
        )
    )
}
```

## Best Practices

### 1. Keep Decorations Focused

Each decoration should handle one specific concern. This makes them more reusable and easier to understand.

### 2. Use Composition for Complex Behavior

Instead of creating complex decorations, compose simple ones:

```kotlin
// Good
val combinedDecoration = logged + withTransaction

// Not as good
val complexDecoration = decorateWith {
    logger().info("Request started")
    transaction { 
        next() 
    }.also {
        logger().info("Request completed")
    }
}
```

### 3. Be Mindful of Decoration Order

The order of decorations matters. For example, if you want to measure the time including transaction overhead, you would do:

```kotlin
val measureTime + withTransaction
```

But if you want to measure only the time spent in the handler, excluding transaction overhead, you would do:

```kotlin
val withTransaction + measureTime
```

### 4. Always Call `next()` Unless Short-Circuiting

If your decoration doesn't call `next()`, the handler will never be executed. This is useful for short-circuiting (e.g., for authentication), but make sure it's intentional.

### 5. Handle Exceptions Appropriately

If your decoration might throw exceptions, consider wrapping the `next()` call in a try-catch block to ensure proper cleanup.

## Real-World Examples

### Authentication and Authorization

Looking at a real-world example from a production codebase:

```kotlin
// Authentication decoration
val authenticated = transformEndpoints {
    with(listOf(accessToken)).decorated {
        when (request[accessToken]) {
            is Authentication.Authenticated -> next()
            is Authentication.Unauthenticated -> UNAUTHORIZED()
        }
    }
}

// Access to user principal and role
val RequestWrapper.principal: UserId get() = 
    (request[accessToken] as Authentication.Authenticated).claims.userId
val RequestWrapper.role: Role get() = 
    (request[accessToken] as Authentication.Authenticated).claims.role

// Condition for checking principal equality
fun principalEquals(param: Parameter<out Any, *>) = condition("Principal equals ${param.name}") {
    if (principal.value == params(param.name)) Successful
    else Failed(FORBIDDEN())
}

// Usage in routes
val usersController = routes {
    withTransaction {
        POST() with body<CreateUserRequest>() isHandledBy createUser
        
        userId / "posts" / {
            authenticated {
                GET() onlyIf principalEquals(userId) isHandledBy getPosts
                POST() onlyIf principalEquals(userId) with body<CreatePostRequest>() isHandledBy createPost
            }
        }
    }
}
```

This example shows how decorations (`withTransaction`, `authenticated`) can be combined with conditions (`principalEquals`) to create a comprehensive authentication and authorization system.

### Logging with Transaction Management

```kotlin
val routes = routes {
    logged {
        withTransaction {
            GET("users").isHandledBy { getUsers() }
            POST("users").with(body<CreateUserRequest>()).isHandledBy { createUser() }
        }
    }
}
```

### API Versioning

```kotlin
fun withApiVersion(version: Int) = decorateWith {
    request.attributes["apiVersion"] = version
    next()
}

// Usage
val routes = routes {
    withApiVersion(1) {
        GET("api/users").isHandledBy { getUsersV1() }
    }
    
    withApiVersion(2) {
        GET("api/users").isHandledBy { getUsersV2() }
    }
}
```

### Rate Limiting

```kotlin
fun rateLimit(maxRequests: Int, perTimeWindow: Duration) = decorateWith {
    val clientIp = request.remoteAddress
    val requestCount = rateLimiter.getRequestCount(clientIp, perTimeWindow)
    
    if (requestCount >= maxRequests) {
        return@decorateWith "Rate limit exceeded. Try again later.".error(429)
    }
    
    rateLimiter.incrementRequestCount(clientIp)
    next()
}

// Usage
rateLimit(100, Duration.ofMinutes(1)) {
    POST("api/messages").isHandledBy { sendMessage() }
}
```

### Caching

```kotlin
fun cache(ttl: Duration) = decorateWith {
    val cacheKey = "${request.method.name}-${request.path}"
    val cachedResponse = cacheService.get(cacheKey)
    
    if (cachedResponse != null) {
        return@decorateWith cachedResponse
    }
    
    val response = next()
    cacheService.put(cacheKey, response, ttl)
    response
}

// Usage
cache(Duration.ofMinutes(5)) {
    GET("api/products").isHandledBy { getProducts() }
}
```

By mastering Snitch's decoration system, you can implement sophisticated middleware chains with minimal code, keeping your routes clean and focused on business logic. Decorations provide a powerful, composable way to handle cross-cutting concerns in your application. 