# Kotlin's Most Elegant HTTP Framework

In today's microservices-driven world, creating robust, well-documented, and maintainable HTTP APIs has become a critical challenge for development teams. **Snitch** emerges as a game-changing solution in this landscape—a lightweight, type-safe Kotlin framework designed to help developers build production-grade HTTP layers with minimal effort while automatically generating comprehensive documentation.

## The Snitch Advantage: Why It's Transforming How We Build APIs

Snitch isn't just another web framework. It's a thoughtfully designed layer built on established embedded web servers like Undertow, offering the perfect balance of performance, type safety, and developer experience. Thanks to Kotlin's inlining capabilities, Snitch delivers exceptional performance without sacrificing readability or maintainability.

### What Sets Snitch Apart From Traditional API Frameworks

- **Zero-Effort Documentation**: Snitch automatically generates complete OpenAPI 3.0 documentation without requiring a single line of documentation code
- **Pure Kotlin Approach**: No reflection, annotations, or code generation for production code
- **Expressive DSL**: Creates the most readable and maintainable API for building HTTP services
- **Strong Type Safety**: Comprehensive parameter validation and domain type mapping
- **Performance-First Design**: As lightweight and fast as the underlying web server
- **Seamless Coroutines Support**: Async by default without sacrificing readability
- **Minimal Overhead**: Small learning curve despite its powerful DSL capabilities

## Getting Started with Snitch in Minutes

Adding Snitch to your project is straightforward:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:4.0.1")
}
```

And creating your first API is just as simple:

```kotlin
import snitch.gson.GsonJsonParser
import snitch.dsl.snitch
import snitch.dsl.routes
import snitch.dsl.response.ok

fun main() {
    snitch(GsonJsonParser)
        .onRoutes {
            GET("/hello") isHandledBy { "world".ok }
        }
        .start()
        .serveDocumenation()
}
```

This minimal example creates a service that:
- Responds with "world" when you make a GET request to `/hello`
- Automatically generates and serves interactive API documentation at `/docs`

## Snitch's Core Features: The Building Blocks of Modern APIs

### 1. Expressive Routing with Intuitive Syntax

Snitch's DSL allows you to define routes in a clear, intuitive way that makes even complex routing structures easy to understand:

```kotlin
val usersController = routes {
    POST() with body<CreateUserRequest>() isHandledBy createUser
    POST("login") with body<LoginRequest>() isHandledBy userLogin

    userId / "posts" / {
        authenticated {
            GET() onlyIf principalEquals(userId) isHandledBy getPosts
            POST() onlyIf principalEquals(userId) with body<CreatePostRequest>() isHandledBy createPost

            GET(postId) isHandledBy getPost
            PUT(postId) with body<UpdatePostRequest>() onlyIf principalEquals(userId) isHandledBy updatePost
            DELETE(postId) onlyIf (principalEquals(userId) or hasAdminRole) isHandledBy deletePost
        }
    }
}
```

The DSL is flexible enough to support different team preferences—routes can be grouped by path, by HTTP method, or in a hybrid approach. This adaptability makes Snitch suitable for teams with different coding styles and organizational preferences.

### 2. Comprehensive Type-Safe Parameter Handling

Snitch's parameter handling system ensures all inputs are validated and mapped to appropriate domain types:

```kotlin
// Path parameters
val userId by path(ofUserId)
val postId by path(ofPostId)

// Query parameters with validation
val limit by query(ofNonNegativeInt(max = 30))
val offset by query(ofNonNegativeInt())
val sort by optionalQuery(ofEnum<Sorting>(), default = NEW)

// Headers with custom validation
val accessToken by header(ofValidAccessToken)

// Custom validators for domain types
val ofUserId = validator<String, UserId> { UserId(UUID.fromString(it)) }
val ofValidAccessToken = stringValidator("valid jwt") { jwt().validate(it) }
```

This approach ensures that by the time your handler code runs, all parameters have been validated and transformed into their proper domain types, eliminating the need for repetitive validation code.

### 3. Powerful Middleware System

Snitch provides a flexible middleware system that simplifies cross-cutting concerns:

```kotlin
// Simple logging middleware
val Router.log get() = decorateWith {
    logger().info("Begin Request: ${request.method.name} ${request.path}")
    next().also {
        logger().info("End Request: ${request.method.name} ${request.path} ${it.statusCode.code}")
    }
}

// Transaction middleware for database operations
val Router.withTransaction get() = decorateWith { 
    transaction { next() } 
}

// Apply middleware to routes
val rootRouter = routes {
    log {
        withTransaction {
            "users" / usersController
            "posts" / postsController
        }
    }
}
```

Middleware can be applied to entire route hierarchies or individual endpoints, giving you precise control over behavior.

### 4. Advanced Access Control with Guards and Conditions

Snitch's guard system provides a clean, expressive syntax for controlling access to endpoints:

```kotlin
// Define conditions
val hasAdminRole = condition("hasAdminRole") {
    when (role) {
        ADMIN -> ConditionResult.Successful
        else -> ConditionResult.Failed("Not an admin".forbidden())
    }
}

val isResourceOwner = condition("isResourceOwner") {
    if (principal.id == request[resourceId]) ConditionResult.Successful
    else ConditionResult.Failed("Not the resource owner".forbidden())
}

// Apply conditions to endpoints
DELETE("resource" / resourceId) onlyIf (isResourceOwner or hasAdminRole) isHandledBy { deleteResource() }
```

Conditions support logical operators for complex access rules and can be applied to entire route hierarchies, providing a powerful yet readable way to implement authorization.

### 5. Automated Documentation That's Always Up-to-Date

Perhaps Snitch's most impressive feature is its ability to generate complete OpenAPI 3.0 documentation with absolutely no additional effort:

```kotlin
snitch(GsonJsonParser)
    .onRoutes(rootRouter)
    .generateDocumentation()
    .servePublicDocumenation()
    .start()
```

Every input, output, parameter, and response code is automatically documented, giving you interactive Swagger UI pages that stay perfectly in sync with your code. This eliminates the documentation drift that plagues many API projects.

### 6. Seamless Integration with Shank for Dependency Injection

Snitch integrates with Shank, a high-performance dependency injection library:

```kotlin
// Define modules with dependencies
object ApplicationModule : ShankModule {
    val logger = single { -> LoggerImpl() }
    val clock = single { -> Clock.systemUTC() }
}

object RepositoryModule : ShankModule {
    val usersRepository = single<UsersRepository> { -> PostgresUsersRepository() }
}

// Use dependencies in handlers
val getUsers by handling {
    val logger = ApplicationModule.logger()
    val usersRepo = RepositoryModule.usersRepository()
    
    logger.info("Fetching all users")
    usersRepo.getUsers().ok
}
```

Shank provides best-in-class performance with strictly type-safe dependency management, built-in cycle detection, and zero reflection overhead—all in a lightweight package.

## Real-World Applications: Where Snitch Shines

Snitch excels in a variety of scenarios:

### Microservices Architecture

In microservices environments, Snitch offers:
- **Light resource footprint**: Runs in as little as 12MB of RAM
- **Fast startup time**: Minimal overhead means services start quickly
- **Automatic documentation**: Each service self-documents its API
- **Strong contract enforcement**: Type safety ensures API contracts are maintained

### High-Performance APIs

When performance is critical, Snitch delivers:
- **Minimal overhead**: Thin layer on top of high-performance web servers
- **Low GC pressure**: Careful design minimizes object creation
- **No reflection**: Zero runtime reflection for request handling
- **Shallow call stack**: Optimized for execution speed

### Teams with Documentation Requirements

For teams that need to maintain API documentation:
- **Zero documentation effort**: Documentation generated automatically
- **Always up-to-date**: Documentation reflects the actual code
- **Interactive Swagger UI**: Test endpoints directly from documentation
- **OpenAPI 3.0 compatibility**: Integrates with API management tools

## Best Practices for Building with Snitch

### 1. Domain-Driven Design

Snitch encourages mapping HTTP inputs to domain types:

```kotlin
// Transform raw string to domain type
val ofUserId = validator<String, UserId> { UserId(UUID.fromString(it)) }
val userId by path(ofUserId)

// In handler, work with domain types
val getUser by handling {
    // userId is already a UserId domain type
    userRepository().getUser(request[userId]).ok
}
```

This approach creates a clean separation between your HTTP layer and domain logic.

### 2. Middleware for Cross-Cutting Concerns

Use middleware for aspects that span multiple endpoints:

```kotlin
// Apply logging, authentication, and transactions
routes {
    log {
        authenticated {
            withTransaction {
                "users" / usersController
            }
        }
    }
}
```

This keeps your handlers focused on business logic rather than infrastructure concerns.

### 3. Granular Access Control

Leverage Snitch's condition system for fine-grained access control:

```kotlin
onlyIf(hasAdminRole) {
    "admin" / adminController
}

"users" / userId / "posts" / {
    GET() isHandledBy getPosts // Anyone can read posts
    POST() onlyIf principalEquals(userId) isHandledBy createPost // Only post owner can create
    DELETE(postId) onlyIf (principalEquals(userId) or hasAdminRole) isHandledBy deletePost // Owner or admin can delete
}
```

This makes your authorization rules explicit and declarative.

## The Future of API Development with Snitch

In an era where API development often involves significant boilerplate and documentation overhead, Snitch offers a refreshing alternative. By combining Kotlin's expressive syntax with automatic documentation generation and strong type safety, Snitch allows developers to focus on what matters most: building great services.

The framework's design philosophy—focused on readability, type safety, and minimal ceremony—represents the future direction of API development tools. As microservices continue to proliferate and API-first development becomes standard practice, tools like Snitch that streamline the development process while enforcing good practices will become increasingly valuable.

## Getting Started with Snitch Today

Ready to transform your API development experience? Here's how to get started:

1. **Add the dependency** to your Gradle build file
2. **Create your first endpoint** with the intuitive DSL
3. **Run your service** and explore the automatically generated documentation
4. **Learn more** through the comprehensive guides and tutorials

Join a growing community of developers who are discovering the power of Snitch for building elegant, performant, and well-documented APIs.

## Conclusion: Why Snitch Exists

The creators of Snitch believe that:
- Code should be as readable as possible
- Simple tasks shouldn't require complex ceremony
- Resources are precious and shouldn't be wasted
- Documentation is crucial but should be generated automatically
- Nobody should ever have to manually edit OpenAPI YAML or JSON files

Whether you're developing microservices, backend APIs, or full applications, Snitch provides the tools you need to create robust, well-documented HTTP layers with minimal effort and maximum readability.

[GitHub Repository](https://github.com/memoizr/snitch) | [Maven Central](https://central.sonatype.com/artifact/io.github.memoizr/snitch-bootstrap) | [Join Discord Community](https://discord.gg/bG6NW3UyxS) 