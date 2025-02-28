# Mastering Snitch Conditions

Conditions are one of Snitch's most powerful features, allowing you to implement sophisticated access control and request validation with minimal code. This tutorial will guide you through everything you need to know about conditions, from basic usage to advanced patterns.

## Understanding Conditions

In Snitch, a condition is a predicate that evaluates a request and determines whether it should proceed or be rejected. Conditions are represented by the `Condition` interface, which has three key components:

1. **Description**: A human-readable description of what the condition checks
2. **Transform function**: A function that can modify an endpoint (usually for documentation purposes)
3. **Check function**: The actual logic that evaluates the request

When a condition is applied to an endpoint using `onlyIf`, it becomes part of the request processing pipeline. If the condition evaluates to `Successful`, the request proceeds; if it evaluates to `Failed`, the request is rejected with the specified error response.

## Basic Condition Usage

The simplest way to use conditions is with the `onlyIf` method on an endpoint:

```kotlin
GET("resource" / resourceId) onlyIf isResourceOwner isHandledBy { getResource() }
```

This ensures that the endpoint will only be accessible if the `isResourceOwner` condition evaluates to `Successful`.

## Creating Custom Conditions

You can create custom conditions using the `condition` factory function:

```kotlin
val hasAdminRole = condition("hasAdminRole") {
    val role = (request[accessToken] as? Authentication.Authenticated)?.claims?.role
    
    when (role) {
        Role.ADMIN -> ConditionResult.Successful
        else -> ConditionResult.Failed("Admin role required".forbidden())
    }
}
```

The first parameter is the description, which will be used in documentation and error messages. The lambda receives a `RequestWrapper` and should return a `ConditionResult`.

### Parameterized Conditions

You can create reusable condition factories that accept parameters:

```kotlin
fun hasMinimumAge(minAge: Int) = condition("hasMinimumAge($minAge)") {
    val userAge = userRepository.getAge(request[userId])
    
    if (userAge >= minAge) {
        ConditionResult.Successful
    } else {
        ConditionResult.Failed("User must be at least $minAge years old".forbidden())
    }
}

// Usage
GET("adult-content") onlyIf hasMinimumAge(18) isHandledBy { getAdultContent() }
```

## Logical Operators

Snitch conditions support three logical operators:

### AND (`and`)

The `and` operator creates a condition that succeeds only if both conditions succeed:

```kotlin
val canAccessResource = isAuthenticated and hasPermission
```

When evaluating an `and` condition, if the first condition fails, the second one is not evaluated (short-circuit evaluation).

### OR (`or`)

The `or` operator creates a condition that succeeds if either condition succeeds:

```kotlin
val canModifyResource = isResourceOwner or hasAdminRole
```

When evaluating an `or` condition, if the first condition succeeds, the second one is not evaluated.

### NOT (`not` or `!`)

The `not` operator inverts a condition:

```kotlin
val isNotLocked = !isResourceLocked
```

You can combine these operators to create complex access rules:

```kotlin
val canEditDocument = isAuthenticated and (isDocumentOwner or hasEditorRole) and !isDocumentLocked
```

## Applying Conditions to Route Hierarchies

You can apply conditions to entire route hierarchies using the `onlyIf` block:

```kotlin
onlyIf(isAuthenticated) {
    GET("profile") isHandledBy { getProfile() }
    
    onlyIf(hasAdminRole) {
        GET("admin/dashboard") isHandledBy { getDashboard() }
        GET("admin/users") isHandledBy { getUsers() }
    }
}
```

In this example, all routes require authentication, and the admin routes additionally require the admin role.

## Short-Circuit Evaluation

Snitch's condition operators use short-circuit evaluation for efficiency:

- For `and`, if the first condition fails, the second is not evaluated
- For `or`, if the first condition succeeds, the second is not evaluated

This is particularly useful when you have conditions with side effects or expensive operations:

```kotlin
// The database query will only run if the user is authenticated
val canAccessResource = isAuthenticated and hasPermissionInDatabase
```

You can test this behavior:

```kotlin
@Test
fun `short-circuits condition evaluation`() {
    var secondConditionEvaluated = false
    
    val trackingCondition = condition("tracking") {
        secondConditionEvaluated = true
        ConditionResult.Successful
    }
    
    given {
        GET("short-circuit")
            .onlyIf(alwaysFalse and trackingCondition)
            .isHandledBy { "".ok }
    } then {
        GET("/short-circuit").expectCode(403)
        assert(!secondConditionEvaluated) { "Second condition should not have been evaluated" }
    }
}
```

## Error Handling and Custom Responses

When a condition fails, it returns a `ConditionResult.Failed` with an error response. You can customize this response:

```kotlin
val isResourceOwner = condition("isResourceOwner") {
    if (principal.id == request[resourceId]) {
        ConditionResult.Successful
    } else {
        ConditionResult.Failed(
            ErrorResponse(
                code = "FORBIDDEN",
                message = "You don't have permission to access this resource",
                details = mapOf("resourceId" to request[resourceId])
            ).error(StatusCodes.FORBIDDEN)
        )
    }
}
```

This allows you to provide detailed, context-specific error messages to clients.

## Best Practices

### 1. Keep Conditions Focused

Each condition should check one specific thing. This makes them more reusable and easier to understand.

### 2. Use Descriptive Names

Choose condition names that clearly describe what they check:

```kotlin
// Good
val hasAdminRole = condition("hasAdminRole") { ... }

// Not as good
val adminCheck = condition("adminCheck") { ... }
```

### 3. Leverage Composition

Build complex access rules by composing simple conditions:

```kotlin
val canEditDocument = isAuthenticated and isDocumentOwner and !isDocumentLocked
```

### 4. Provide Helpful Error Messages

When a condition fails, the error message should help the client understand why:

```kotlin
ConditionResult.Failed("Resource not found or you don't have permission to access it".forbidden())
```

### 5. Document Conditions

Use the description parameter to document what the condition checks:

```kotlin
val hasPermission = condition("User has permission to access the resource") { ... }
```

This description will appear in the generated API documentation.

## Real-World Examples

### Authentication and Authorization

```kotlin
// Authentication
val isAuthenticated = condition("isAuthenticated") {
    when (request[accessToken]) {
        is Authentication.Authenticated -> ConditionResult.Successful
        else -> ConditionResult.Failed("Authentication required".unauthorized())
    }
}

// Authorization
val hasAdminRole = condition("hasAdminRole") {
    val auth = request[accessToken] as? Authentication.Authenticated
        ?: return@condition ConditionResult.Failed("Authentication required".unauthorized())
    
    when (auth.claims.role) {
        Role.ADMIN -> ConditionResult.Successful
        else -> ConditionResult.Failed("Admin role required".forbidden())
    }
}

// Resource ownership
fun isResourceOwner(resourceIdParam: Parameter<String, *>) = condition("isResourceOwner") {
    val auth = request[accessToken] as? Authentication.Authenticated
        ?: return@condition ConditionResult.Failed("Authentication required".unauthorized())
    
    if (auth.claims.userId == request[resourceIdParam]) {
        ConditionResult.Successful
    } else {
        ConditionResult.Failed("You don't own this resource".forbidden())
    }
}

// Usage
val routes = routes {
    onlyIf(isAuthenticated) {
        GET("profile") isHandledBy { getProfile() }
        
        "resources" / resourceId / {
            GET() onlyIf isResourceOwner(resourceId) isHandledBy { getResource() }
            PUT() onlyIf (isResourceOwner(resourceId) or hasAdminRole) isHandledBy { updateResource() }
            DELETE() onlyIf (isResourceOwner(resourceId) or hasAdminRole) isHandledBy { deleteResource() }
        }
        
        onlyIf(hasAdminRole) {
            GET("admin/dashboard") isHandledBy { getDashboard() }
            GET("admin/users") isHandledBy { getUsers() }
        }
    }
}
```

### Rate Limiting

```kotlin
fun rateLimit(maxRequests: Int, perTimeWindow: Duration) = condition("rateLimit($maxRequests per $perTimeWindow)") {
    val clientIp = request.remoteAddress
    val requestCount = rateLimiter.getRequestCount(clientIp, perTimeWindow)
    
    if (requestCount <= maxRequests) {
        ConditionResult.Successful
    } else {
        ConditionResult.Failed(
            ErrorResponse(
                code = "TOO_MANY_REQUESTS",
                message = "Rate limit exceeded. Try again later.",
                details = mapOf(
                    "maxRequests" to maxRequests,
                    "timeWindow" to perTimeWindow.toString(),
                    "retryAfter" to rateLimiter.getRetryAfter(clientIp)
                )
            ).error(StatusCodes.TOO_MANY_REQUESTS)
        )
    }
}

// Usage
onlyIf(rateLimit(100, Duration.ofMinutes(1))) {
    POST("api/v1/messages") isHandledBy { sendMessage() }
}
```

### Feature Flags

```kotlin
fun featureEnabled(featureName: String) = condition("featureEnabled($featureName)") {
    if (featureFlagService.isEnabled(featureName)) {
        ConditionResult.Successful
    } else {
        ConditionResult.Failed("Feature not available".notFound())
    }
}

// Usage
GET("new-feature") onlyIf featureEnabled("new-feature") isHandledBy { useNewFeature() }
```

By mastering Snitch's condition system, you can implement sophisticated access control and request validation with minimal code, keeping your routes clean and focused on business logic. 