# Before and After Actions

The Before and After action mechanism in Snitch provides a powerful way to run code around your route handlers. It allows you to execute logic before a request is processed and after a response is generated, which is useful for cross-cutting concerns such as logging, authentication, authorization, and performance monitoring.

## Understanding Before and After Actions

Before and After actions are similar to middleware in other frameworks. They allow you to:

1. **Before Actions**: Execute code before the route handler is called
2. **After Actions**: Execute code after the route handler has completed and a response is generated

These actions have full access to the request context, including path, query, and header parameters, and can also modify the response returned by the handler.

## Route-Level Actions

Route-level actions are applied to individual routes, allowing for fine-grained control over which routes need specific pre- or post-processing.

### Defining Before Actions

```kotlin
GET("users" / userId)
    .doBefore { 
        // Code to run before the handler
        logger.info("Accessing user: ${request[userId]}")
    }
    .isHandledBy {
        // Handler code
        userRepository.getUser(request[userId]).ok
    }
```

### Defining After Actions

```kotlin
GET("users" / userId)
    .doAfter { 
        // Code to run after the handler has generated a response
        logger.info("User access completed with status: ${response.statusCode}")
    }
    .isHandledBy {
        // Handler code
        userRepository.getUser(request[userId]).ok
    }
```

### Combining Before and After Actions

You can chain multiple before and after actions on a single route:

```kotlin
GET("users" / userId)
    .doBefore { logger.info("Request started") }
    .doBefore { metrics.incrementRequestCount() }
    .doAfter { metrics.recordResponseTime() }
    .doAfter { logger.info("Request completed") }
    .isHandledBy {
        userRepository.getUser(request[userId]).ok
    }
```

## Global Actions

Global actions let you apply the same logic to multiple routes or across your entire application, reducing code duplication.

### Applying to Multiple Routes

```kotlin
applyToAll_({
    GET("users" / userId) isHandledBy getUserHandler
    POST("users") with body<CreateUserRequest>() isHandledBy createUserHandler
    PUT("users" / userId) with body<UpdateUserRequest>() isHandledBy updateUserHandler
}) {
    doBefore { 
        logger.info("${request.method} ${request.path} started")
    }
    doAfter { 
        logger.info("${request.method} ${request.path} completed with status ${response.statusCode}")
    }
}
```

## Execution Order

Understanding the execution order of before and after actions is crucial for building correct, predictable behavior.

### Order for Before Actions

1. **Before actions execute in reverse declaration order** (last declared, first executed)
2. **Global before actions execute before route-level before actions**

For example:

```kotlin
applyToAll_({
    GET("foo")
        .doBefore { /* Route before 1 */ }
        .doBefore { /* Route before 2 */ }
        .isHandledBy { /* Handler */ }
}) {
    doBefore { /* Global before 1 */ }
    doBefore { /* Global before 2 */ }
}
```

Execution order:
1. Global before 2
2. Global before 1
3. Route before 2
4. Route before 1
5. Handler

### Order for After Actions

1. **After actions execute in declaration order** (first declared, first executed)
2. **Route-level after actions execute before global after actions**

For example:

```kotlin
applyToAll_({
    GET("foo")
        .doAfter { /* Route after 1 */ }
        .doAfter { /* Route after 2 */ }
        .isHandledBy { /* Handler */ }
}) {
    doAfter { /* Global after 1 */ }
    doAfter { /* Global after 2 */ }
}
```

Execution order:
1. Handler
2. Route after 1
3. Route after 2
4. Global after 1
5. Global after 2

### Complete Execution Flow

The complete execution flow for a request is:

1. Global before actions (in reverse declaration order)
2. Route-level before actions (in reverse declaration order)
3. Route handler
4. Route-level after actions (in declaration order)
5. Global after actions (in declaration order)

## Error Handling

The behavior of before and after actions differs slightly when errors occur:

### Exceptions in Before Actions

When an exception occurs in a before action:

1. The exception is caught and processed by any registered exception handlers
2. The route handler is **not** executed
3. After actions are **not** executed automatically (as of the current implementation)

```kotlin
GET("foo")
    .doBefore { 
        throw RuntimeException("Error in before action")
    }
    .doAfter { 
        // Currently not executed when before action throws an exception
    }
    .isHandledBy {
        // Not executed when before action throws an exception
    }
```

### Exceptions in Handlers

When an exception occurs in a route handler:

1. The exception is caught and processed by any registered exception handlers
2. After actions are **not** executed automatically (as of the current implementation)

```kotlin
// Register exception handler
handleException(RuntimeException::class) {
    logger.error("Exception caught: ${it.message}")
    "Error occurred".serverError()
}

GET("foo")
    .doBefore { /* Executes normally */ }
    .doAfter { 
        // Currently not executed when handler throws an exception
    }
    .isHandledBy {
        throw RuntimeException("Error in handler")
    }
```

> **Note**: According to the test file, there's a TODO comment indicating that after actions don't currently run in case of exceptions in before actions or handlers, but this behavior may be implemented in future versions.

## Common Use Cases

Before and after actions are ideal for many cross-cutting concerns:

### Logging

```kotlin
val Router.withLogging get() = transformEndpoints {
    doBefore { 
        logger.info("Request started: ${request.method} ${request.path}")
        request.attributes["startTime"] = System.currentTimeMillis()
    }
    doAfter {
        val startTime = request.attributes["startTime"] as Long
        val duration = System.currentTimeMillis() - startTime
        logger.info("Request completed: ${request.method} ${request.path} - ${response.statusCode} (${duration}ms)")
    }
}

// Usage
withLogging {
    GET("users") isHandledBy { /* handler */ }
    POST("users") isHandledBy { /* handler */ }
}
```

### Authentication

```kotlin
val accessToken by header(validJwtValidator)

val Router.authenticated get() = transformEndpoints {
    with(queries(accessToken)).doBefore {
        val token = request[accessToken]
        if (token is Authentication.Unauthenticated) {
            return@doBefore "Unauthorized".unauthorized()
        }
    }
}

// Usage
authenticated {
    GET("profile") isHandledBy getUserProfile
    PUT("settings") isHandledBy updateUserSettings
}
```

### Request Metrics

```kotlin
val Router.withMetrics get() = transformEndpoints {
    doBefore {
        metrics.incrementRequestCount(request.path)
        request.attributes["startTime"] = System.nanoTime()
    }
    doAfter {
        val duration = System.nanoTime() - (request.attributes["startTime"] as Long)
        metrics.recordResponseTime(request.path, duration)
        metrics.recordStatusCode(request.path, response.statusCode.code)
    }
}
```

### Database Transactions

```kotlin
val Router.withTransaction get() = transformEndpoints {
    doBefore {
        transaction.begin()
    }
    doAfter {
        if (response.isSuccessful()) {
            transaction.commit()
        } else {
            transaction.rollback()
        }
    }
}

// Usage
withTransaction {
    POST("orders") isHandledBy createOrder
    PUT("orders" / orderId) isHandledBy updateOrder
}
```

## Best Practices

### 1. Keep Before and After Actions Focused

Each before and after action should have a single responsibility. Instead of having one big action that does multiple things, chain smaller, focused actions:

```kotlin
// Good
GET("users")
    .doBefore { validateRequest() }
    .doBefore { authenticate() }
    .doBefore { authorize() }
    .doAfter { logResponse() }
    .doAfter { collectMetrics() }
    .isHandledBy { /* handler */ }

// Avoid
GET("users")
    .doBefore { 
        validateRequest()
        authenticate()
        authorize()
    }
    .doAfter { 
        logResponse()
        collectMetrics()
    }
    .isHandledBy { /* handler */ }
```

### 2. Use Global Actions for Cross-Cutting Concerns

When actions need to be applied across multiple routes or your entire application, use global actions to reduce duplication:

```kotlin
// Error logging applied to all routes
applyToAll_({
    // All your routes
}) {
    doAfter { 
        if (response.statusCode.isError()) {
            logger.error("Error response: ${response.statusCode} - ${response.body}")
        }
    }
}
```

### 3. Be Careful with Response Modification

After actions have the ability to modify the response. Use this power with care:

```kotlin
GET("users")
    .doAfter { 
        // Only modify the response when needed
        if (response.statusCode.isSuccess() && response is JsonResponse) {
            // Add additional info to JSON response
            response.addAttribute("serverTime", System.currentTimeMillis())
        }
    }
    .isHandledBy { /* handler */ }
```

### 4. Handle Errors Explicitly

Be explicit about error handling in your before and after actions:

```kotlin
GET("users")
    .doBefore { 
        try {
            // Risky operation
        } catch (e: Exception) {
            logger.error("Error in before action", e)
            return@doBefore "An error occurred".serverError()
        }
    }
    .isHandledBy { /* handler */ }
```

### 5. Be Mindful of Execution Order

Remember that before actions execute in reverse order and after actions execute in declaration order:

```kotlin
GET("users")
    // Executes third
    .doBefore { logger.info("Authorization check") }
    // Executes second
    .doBefore { logger.info("Authentication check") }
    // Executes first
    .doBefore { logger.info("Request validation") }
    
    // Executes first after handler
    .doAfter { logger.info("Log response") }
    // Executes second after handler
    .doAfter { logger.info("Collect metrics") }
    
    .isHandledBy { /* handler */ }
```

## Conclusion

The Before and After action mechanism in Snitch provides a powerful way to organize cross-cutting concerns in your HTTP application. By understanding how these actions are executed and how they interact with exception handling, you can build clean, maintainable, and robust applications.

Remember that actions should be focused, reusable, and predictable. Use global actions for common functionality and route-level actions for specific requirements. Be aware of the current limitations in error handling, and always consider the execution order when designing your action chains.