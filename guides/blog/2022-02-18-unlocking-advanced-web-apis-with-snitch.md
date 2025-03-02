---
slug: unlocking-advanced-web-apis-with-snitch
title: Unlocking Advanced Web APIs with Snitch
authors: [snitch-team]
tags: [snitch, web-development, kotlin, apis, dsl]
---

# Unlocking Advanced Web APIs with Snitch

Building production-grade HTTP APIs can be complex and time-consuming. Many frameworks offer simplicity at the cost of readability or performance when systems grow beyond simple examples. Today, I'm excited to introduce you to **Snitch**: a Kotlin HTTP framework that prioritizes readability and maintainability while delivering exceptional performance and a powerful feature set.

<!-- truncate -->

## The Essence of Snitch

Snitch was created to solve a fundamental problem: as web APIs grow more complex, codebase readability often suffers. Many frameworks prioritize conventions over explicit configuration, which can lead to obscure runtime errors and steep learning curves for new team members.

Here's what makes Snitch different:

- **Expressive, readable DSL** that makes complex route hierarchies understandable at a glance
- **Strong type safety** that catches errors at compile time rather than runtime
- **Minimal overhead** by leveraging Kotlin's inline functions and a thin layer over high-performance servers
- **Automated documentation** generation without additional configuration
- **No reflection magic** in production code, making it easier to understand and debug

For many developers, that "aha!" moment with Snitch comes when they first see how explicitly yet concisely they can model intricate API structures:

```kotlin
val usersController = routes {
    POST() with body<CreateUserRequest>() isHandledBy createUser
    
    userId / "posts" / {
        authenticated {
            GET() onlyIf principalEquals(userId) isHandledBy getPosts
            POST() onlyIf principalEquals(userId) with body<CreatePostRequest>() isHandledBy createPost
            
            GET(postId) isHandledBy getPost
            DELETE(postId) onlyIf (principalEquals(userId) or hasAdminRole) isHandledBy deletePost
        }
    }
}
```

This declarative style strikes an impressive balance between readability and expressiveness, making it immediately clear what routes are available and what security constraints apply.

## The Validation System: Transform Raw Inputs into Domain Types

Snitch's validator system addresses a critical challenge in HTTP API development: safely converting raw string inputs into strongly-typed domain objects. Let's look at what makes this system powerful:

```kotlin
// Define custom domain types
data class UserId(val value: String)
data class PostId(val value: String)

// Create validators that validate and transform raw inputs
val ofUserId = validator<String, UserId>("valid user ID", """^[a-zA-Z0-9]{8,12}$""".toRegex()) {
    UserId(it)
}

// Use validators with parameters
val userId by path(ofUserId)
val postId by path(ofPostId)

// Access validated, typed parameters in handlers
val getPost by handling {
    val user: UserId = request[userId]
    val post: PostId = request[postId]
    
    postRepository.findPost(user, post).ok
}
```

With this approach, validations happen before your handler code runs, so you're always working with properly validated, domain-specific types. If validation fails, the framework returns appropriate 400-level responses automatically, with descriptive error messages that help API consumers fix their requests.

## Before and After Actions: Tackle Cross-Cutting Concerns Elegantly

One of Snitch's newer features that I find particularly elegant is its **before and after** action system. This lets you execute code around your handlers in a clean, composable way:

```kotlin
GET("users" / userId)
    .doBefore { 
        // Authentication check
        if (request[accessToken] is Authentication.Unauthenticated) {
            return@doBefore "Unauthorized".unauthorized()
        }
    }
    .doBefore {
        // Logging
        logger.info("User access: ${request[userId]}")
        request.attributes["startTime"] = System.currentTimeMillis()
    }
    .doAfter { 
        // Performance tracking
        val duration = System.currentTimeMillis() - (request.attributes["startTime"] as Long)
        metrics.recordRequestTime(request.path, duration)
    }
    .isHandledBy {
        userRepository.findUser(request[userId]).ok
    }
```

What's fascinating about this approach is its flexibility and readability. The before/after actions have full access to the request context, can short-circuit execution with early responses, and can be composed together in intuitive ways. For cross-cutting concerns like logging, metrics, authentication, and authorization, this provides a clean separation of those aspects from your core business logic.

## Conditions: Sophisticated Access Control Made Simple

Snitch's condition system is another gem worth exploring. It allows for incredibly expressive access control rules that are both readable and maintainable:

```kotlin
val isResourceOwner = condition("isResourceOwner") {
    if (principal.id == request[resourceId]) ConditionResult.Successful
    else ConditionResult.Failed("Not the resource owner".forbidden())
}

val hasAdminRole = condition("hasAdminRole") {
    when (role) {
        ADMIN -> ConditionResult.Successful
        else -> ConditionResult.Failed("Not an admin".forbidden())
    }
}

// Apply conditions to endpoints
DELETE("posts" / postId) onlyIf (isResourceOwner or hasAdminRole) isHandledBy deletePost
```

The most impressive aspect here is the support for logical operators (`and`, `or`, `not`) that work exactly as you'd expect, making complex access control rules both expressive and maintainable.

## Decoration: Reusable Middleware Patterns

The decoration system in Snitch provides a flexible way to wrap behavior around route handlers:

```kotlin
val withLogging = decorateWith {
    logger.info("Begin Request: ${request.method} ${request.path}")
    next().also {
        logger.info("End Request: ${request.method} ${request.path} ${it.statusCode} (${System.currentTimeMillis() - startTime}ms)")
    }
}

val withTransaction = decorateWith {
    transaction {
        next()
    }
}

// Apply to routes
withLogging {
    withTransaction {
        POST("orders") isHandledBy createOrder
    }
}
```

The `next()` function is the key - it executes the next decoration in the chain or the handler itself, allowing for pre and post-processing while maintaining a clean control flow.

## Everything Unified by Documentation

Perhaps the most impressive feature - one that developers typically dread implementing - is automatic API documentation. Snitch generates OpenAPI 3.0 documentation without any additional configuration:

```kotlin
snitch(GsonJsonParser)
    .onRoutes(myRoutes)
    .generateDocumentation()
    .servePublicDocumentation()
    .start()
```

With this minimal setup, you get a full Swagger UI that accurately reflects your API, including:
- All routes and their HTTP methods
- Path, query, and header parameters with their validators and descriptions
- Request and response body schemas
- Authentication requirements
- Possible response status codes

The magic here is that this documentation is derived directly from your code, so it's always accurate and up-to-date.

## Performance Without Compromise

Beyond the elegant API, Snitch delivers impressive performance. By using Undertow as the default embedded server and carefully avoiding reflection and excessive object creation, it achieves near-native performance while maintaining its expressive DSL.

A typical Snitch application has a tiny memory footprint (as low as 12MB of RAM on top of the JVM) and minimal startup time, making it suitable for everything from microservices to Android applications.

## Conclusion: A Framework That Grows With You

What I find most compelling about Snitch is how it scales with complexity. Simple APIs remain simple, but as your requirements grow more sophisticated - with nested routes, complex access control, detailed validation, and cross-cutting concerns - the code remains readable and maintainable.

Snitch achieves this by providing powerful abstractions that are composable and explicit, avoiding the "magic" that often makes frameworks hard to reason about as applications grow.

If you're building HTTP APIs in Kotlin and value both expressiveness and type safety, Snitch deserves a serious look. Its combination of a readable DSL, sophisticated features like validators and conditions, and exceptional performance makes it a compelling choice for professional API development.

To get started with Snitch, check out the [comprehensive tutorials](/docs/in%20depth/Mastering-Snitch-Parameters) in our documentation section, or dive right into the [example application](https://github.com/memoizr/snitch/tree/master/example) on GitHub.