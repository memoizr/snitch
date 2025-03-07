# Shank in Action: Building a Real-World Application

This tutorial demonstrates how to use Shank, Snitch's powerful dependency injection library, to build a maintainable and testable real-world application. We'll create a blog platform with user authentication, post management, and comments.

## What is Shank?

[Shank](https://github.com/memoizr/shank) is a lightweight, high-performance dependency injection library for Kotlin that integrates seamlessly with Snitch. It provides:

- **Industry-leading performance**: Unmatched startup and runtime performance
- **Zero reflection**: No runtime overhead or class scanning
- **Strict type safety**: Will never throw runtime exceptions related to types
- **Polymorphism support**: Interface-based dependency injection
- **Testing support**: Easy factory overriding for testing

## Project Setup

Let's start by setting up our project structure:

```kotlin
// build.gradle.kts
plugins {
    kotlin("jvm") version "1.9.0"
    application
}

dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:4.0.1")
    implementation("org.jetbrains.exposed:exposed-core:0.41.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.41.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.41.1")
    implementation("org.postgresql:postgresql:42.6.0")
    implementation("de.mkammerer:argon2-jvm:2.11")
    
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("io.mockk:mockk:1.13.5")
}
```

## Domain Model

First, let's define our domain models:

```kotlin
// src/main/kotlin/blog/domain/models.kt
package blog.domain

import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID,
    val username: String,
    val email: String,
    val createdAt: Instant
)

data class Post(
    val id: UUID,
    val title: String,
    val content: String,
    val authorId: UUID,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class Comment(
    val id: UUID,
    val postId: UUID,
    val authorId: UUID,
    val content: String,
    val createdAt: Instant
)
```

## Setting Up Shank Modules

Now, let's organize our application using Shank modules. This is where Shank's elegance and convenience really shine:

```kotlin
// src/main/kotlin/blog/infrastructure/ApplicationModule.kt
package blog.infrastructure

import blog.domain.Clock
import blog.domain.SystemClock
import snitch.shank.ShankModule
import snitch.shank.new
import snitch.shank.single
import java.time.Instant

object ApplicationModule : ShankModule {
    // Core application dependencies
    val clock = single<Clock> { -> SystemClock() }
    
    // Creates a new timestamp each time it's called
    val now = new { -> Instant.now(clock().utc()) }
    
    // Logger is a singleton
    val logger = single { -> LoggerFactory.getLogger("blog-app") }
}
```

Notice how we're using `single` for dependencies that should have only one instance, and `new` for dependencies that should be created fresh each time they're requested. This is one of Shank's conveniences - clear, explicit scoping.

Let's add our database module:

```kotlin
// src/main/kotlin/blog/infrastructure/DatabaseModule.kt
package blog.infrastructure

import org.jetbrains.exposed.sql.Database
import snitch.shank.ShankModule
import snitch.shank.single

object DatabaseModule : ShankModule {
    // Database connection is a singleton
    val connection = single { ->
        Database.connect(
            "jdbc:postgresql://localhost:5432/blog",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgres"
        )
    }
    
    // Database wrapper is a singleton
    val database = single { -> PostgresDatabase(connection()) }
}
```

And our repositories:

```kotlin
// src/main/kotlin/blog/infrastructure/RepositoryModule.kt
package blog.infrastructure

import blog.domain.repositories.*
import blog.infrastructure.repositories.*
import snitch.shank.ShankModule
import snitch.shank.single

object RepositoryModule : ShankModule {
    // Bind implementations to interfaces
    val userRepository = single<UserRepository> { -> 
        PostgresUserRepository(
            DatabaseModule.database(),
            ApplicationModule.now()
        ) 
    }
    
    val postRepository = single<PostRepository> { -> 
        PostgresPostRepository(
            DatabaseModule.database(),
            ApplicationModule.now()
        ) 
    }
    
    val commentRepository = single<CommentRepository> { -> 
        PostgresCommentRepository(
            DatabaseModule.database(),
            ApplicationModule.now()
        ) 
    }
}
```

Finally, let's add our security module:

```kotlin
// src/main/kotlin/blog/infrastructure/SecurityModule.kt
package blog.infrastructure

import de.mkammerer.argon2.Argon2Factory
import snitch.shank.ShankModule
import snitch.shank.single
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateCrtKey

object SecurityModule : ShankModule {
    // Generate a key pair for JWT signing
    val keyPair = single { ->
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        keyPairGenerator.genKeyPair()
    }

    // Extract keys from the key pair
    val privateKey = single { -> keyPair().private as RSAPrivateCrtKey }
    val publicKey = single { -> keyPair().public }
    
    // Password hashing
    val argon = single { -> Argon2Factory.create() }
    val passwordHasher = single<PasswordHasher> { -> Argon2PasswordHasher(argon()) }
    
    // JWT management
    val jwtManager = single { -> JWTManager(privateKey(), publicKey()) }
}
```

## The Power of Shank: Service Layer

Now, let's create our service layer. This is where Shank's convenience becomes evident - we can easily compose our services using dependencies from different modules:

```kotlin
// src/main/kotlin/blog/application/ServiceModule.kt
package blog.application

import blog.application.services.*
import blog.infrastructure.ApplicationModule
import blog.infrastructure.RepositoryModule
import blog.infrastructure.SecurityModule
import snitch.shank.ShankModule
import snitch.shank.single

object ServiceModule : ShankModule {
    // User service with dependencies from multiple modules
    val userService = single<UserService> { -> 
        UserServiceImpl(
            RepositoryModule.userRepository(),
            SecurityModule.passwordHasher(),
            SecurityModule.jwtManager(),
            ApplicationModule.now()
        ) 
    }
    
    // Post service
    val postService = single<PostService> { -> 
        PostServiceImpl(
            RepositoryModule.postRepository(),
            RepositoryModule.userRepository(),
            ApplicationModule.now()
        ) 
    }
    
    // Comment service
    val commentService = single<CommentService> { -> 
        CommentServiceImpl(
            RepositoryModule.commentRepository(),
            RepositoryModule.postRepository(),
            ApplicationModule.now()
        ) 
    }
}
```

## API Layer with Snitch and Shank

Now, let's create our API layer using Snitch and Shank together:

```kotlin
// src/main/kotlin/blog/api/Router.kt
package blog.api

import blog.application.ServiceModule
import blog.domain.exceptions.NotFoundException
import blog.domain.exceptions.UnauthorizedException
import snitch.dsl.*
import snitch.dsl.response.*
import snitch.parsers.GsonJsonParser
import snitch.router.decorateWith
import snitch.service.RoutedService
import snitch.undertow.snitch

// Request/response models
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val token: String)

data class CreatePostRequest(val title: String, val content: String)
data class PostResponse(val id: String, val title: String, val content: String, val author: String)

// Path parameters
val userId by path()
val postId by path()
val commentId by path()

// Header parameters for authentication
val accessToken by header(
    condition = validAccessToken,
    name = "Authorization",
    description = "Bearer token for authentication"
)

// Validator for access token
val validAccessToken = stringValidator { token ->
    if (token.startsWith("Bearer ")) {
        try {
            // Use jwtManager from SecurityModule to validate the token
            val jwtManager = SecurityModule.jwtManager()
            val userId = jwtManager.validateToken(token.substring(7))
            Authentication.Authenticated(userId)
        } catch (e: Exception) {
            Authentication.InvalidToken
        }
    } else {
        Authentication.MissingToken
    }
}

// Authentication result model
sealed interface Authentication {
    data class Authenticated(val userId: String) : Authentication
    interface Unauthenticated : Authentication
    object InvalidToken : Unauthenticated
    object MissingToken : Unauthenticated
}

// Authentication middleware using Shank with proper parameter declaration
val authenticated = decorateWith(accessToken) {
    when (val auth = request[accessToken]) {
        is Authentication.Authenticated -> {
            // Set user ID in request attributes for use in handlers
            request.attributes["userId"] = auth.userId
            next()
        }
        is Authentication.Unauthenticated -> "Authentication required".unauthorized()
    }
}

// Main router
val router = routes {
    "api" / {
        "auth" / {
            // Login endpoint
            POST("login") with body<LoginRequest>() isHandledBy {
                val userService = ServiceModule.userService()
                try {
                    val token = userService.login(body.username, body.password)
                    LoginResponse(token).ok
                } catch (e: UnauthorizedException) {
                    "Invalid credentials".unauthorized()
                }
            }
        }
        
        "posts" / {
            // Get all posts
            GET() isHandledBy {
                val postService = ServiceModule.postService()
                postService.getAllPosts().ok
            }
            
            // Get post by ID
            postId / {
                GET() isHandledBy {
                    val postService = ServiceModule.postService()
                    try {
                        val id = request[postId]
                        postService.getPostById(id).ok
                    } catch (e: NotFoundException) {
                        "Post not found".notFound()
                    }
                }
            }
            
            // Protected routes
            authenticated {
                // Create post
                POST() with body<CreatePostRequest>() isHandledBy {
                    val postService = ServiceModule.postService()
                    val userId = request.attributes["userId"] as String
                    val post = postService.createPost(userId, body.title, body.content)
                    post.created
                }
                
                // Update post
                postId / {
                    PUT() with body<CreatePostRequest>() isHandledBy {
                        val postService = ServiceModule.postService()
                        val userId = request.attributes["userId"] as String
                        val id = request[postId]
                        try {
                            val post = postService.updatePost(id, userId, body.title, body.content)
                            post.ok
                        } catch (e: NotFoundException) {
                            "Post not found".notFound()
                        } catch (e: UnauthorizedException) {
                            "Not authorized to update this post".forbidden()
                        }
                    }
                    
                    // Delete post
                    DELETE() isHandledBy {
                        val postService = ServiceModule.postService()
                        val userId = request.attributes["userId"] as String
                        val id = request[postId]
                        try {
                            postService.deletePost(id, userId)
                            "Post deleted".ok
                        } catch (e: NotFoundException) {
                            "Post not found".notFound()
                        } catch (e: UnauthorizedException) {
                            "Not authorized to delete this post".forbidden()
                        }
                    }
                }
            }
        }
    }
}
```

## Application Entry Point

Finally, let's create our application entry point:

```kotlin
// src/main/kotlin/blog/Application.kt
package blog

import blog.api.router
import blog.infrastructure.DatabaseModule
import snitch.config.SnitchConfig
import snitch.parsers.GsonJsonParser
import snitch.undertow.snitch

fun main() {
    // Initialize database
    val database = DatabaseModule.database()
    database.createSchema()
    
    // Start the service
    snitch(GsonJsonParser, SnitchConfig(port = 3000))
        .onRoutes(router)
        .handleExceptions()
        .start()
        .serveDocumenation()
}
```

## Testing with Shank

One of Shank's most convenient features is its support for testing. Let's see how to test our service layer:

```kotlin
// src/test/kotlin/blog/application/services/UserServiceTest.kt
package blog.application.services

import blog.domain.User
import blog.domain.repositories.UserRepository
import blog.infrastructure.ApplicationModule
import blog.infrastructure.RepositoryModule
import blog.infrastructure.SecurityModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals

class UserServiceTest {
    private val mockUserRepository = mockk<UserRepository>()
    private val mockPasswordHasher = mockk<PasswordHasher>()
    private val mockJwtManager = mockk<JwtManager>()
    
    @BeforeEach
    fun setup() {
        // Override real implementations with mocks
        RepositoryModule.userRepository.overrideFactory { -> mockUserRepository }
        SecurityModule.passwordHasher.overrideFactory { -> mockPasswordHasher }
        SecurityModule.jwtManager.overrideFactory { -> mockJwtManager }
        
        // Set up a fixed timestamp for testing
        val fixedInstant = Instant.parse("2023-01-01T00:00:00Z")
        ApplicationModule.now.overrideFactory { -> fixedInstant }
    }
    
    @AfterEach
    fun tearDown() {
        // Restore original implementations
        RepositoryModule.userRepository.restore()
        SecurityModule.passwordHasher.restore()
        SecurityModule.jwtManager.restore()
        ApplicationModule.now.restore()
    }
    
    @Test
    fun `login should return token when credentials are valid`() {
        // Arrange
        val username = "testuser"
        val password = "password123"
        val hashedPassword = "hashed_password"
        val userId = UUID.randomUUID()
        val token = "jwt_token"
        
        val user = User(
            id = userId,
            username = username,
            email = "test@example.com",
            createdAt = Instant.now()
        )
        
        every { mockUserRepository.findByUsername(username) } returns user
        every { mockPasswordHasher.verify(hashedPassword, password) } returns true
        every { mockJwtManager.createToken(userId.toString()) } returns token
        
        // Act
        val userService = ServiceModule.userService()
        val result = userService.login(username, password)
        
        // Assert
        assertEquals(token, result)
        verify { mockUserRepository.findByUsername(username) }
        verify { mockPasswordHasher.verify(hashedPassword, password) }
        verify { mockJwtManager.createToken(userId.toString()) }
    }
}
```

## Why Shank is Convenient

As you've seen throughout this tutorial, Shank offers several conveniences that make it an excellent choice for dependency injection in Snitch applications:

1. **Explicit Dependencies**: Dependencies are clearly defined and easy to trace, making your code more maintainable.

2. **No Magic**: Unlike other DI frameworks, Shank doesn't use reflection or annotations, making it faster and more predictable.

3. **Type Safety**: Shank leverages Kotlin's type system to ensure type safety at compile time.

4. **Testability**: Shank's ability to override dependencies makes testing straightforward and reliable.

5. **Performance**: Shank's lightweight design ensures minimal overhead, making it ideal for high-performance applications.

6. **Modularity**: Shank encourages organizing your code into cohesive modules, improving code organization.

7. **Flexibility**: Shank supports various dependency scopes (singleton, factory, scoped), giving you control over object lifecycles.

## Conclusion

In this tutorial, we've built a complete blog application using Snitch and Shank. We've seen how Shank's convenient dependency injection system makes it easy to:

- Organize code into logical modules
- Manage dependencies between different parts of the application
- Test components in isolation
- Create maintainable and flexible code

Shank's simplicity and power make it an excellent choice for dependency injection in Kotlin applications, especially when combined with Snitch's elegant routing and request handling.

## Next Steps

To further explore Shank and Snitch:

1. Check out the [Shank Patterns](./ShankPatterns.md) guide for advanced usage patterns
2. Explore the [Using Shank](./UsingShank.md) guide for more details on Shank's features
3. Look at the example project in the repository for a complete working example 