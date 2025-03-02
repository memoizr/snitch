# Using Shank with Snitch

[Shank](https://github.com/memoizr/shank) is a lightweight dependency injection (DI) library for Kotlin that integrates seamlessly with Snitch. This guide covers how to use Shank effectively in your Snitch applications.

## Introduction to Shank

Shank provides best-in-class performance among all dependency injection frameworks on the JVM, with a simple, strictly type-safe approach that eliminates common pitfalls. Its remarkable features include:

- **Industry-leading performance**: Unmatched startup and runtime performance
- **Zero reflection**: No runtime overhead or class scanning
- **Cycle detection**: Built-in circular dependency detection using only the Kotlin compiler
- **Strict type safety**: Will never throw runtime exceptions related to types
- **Runtime resolution**: All dependencies are resolved at runtime, supporting hot reloading
- **Polymorphism support**: Interface-based dependency injection
- **Scoped dependencies**: Context-aware dependency scoping
- **Parameterized injection**: Dependencies can be scoped to injection parameters
- **Testing support**: Easy factory overriding for testing
- **Lightweight**: Only 300kb, with virtually no startup overhead
- **Lazy initialization**: Modules are objects initialized only when needed
- **Infinite extensibility**: Supports any custom dependency resolution strategy

## Adding Shank to Your Project

The Shank library is included as a transitive dependency when you include Snitch, so no additional dependency is needed:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:1.0.0")
}
```

## Core Concepts

### ShankModule

The primary building block in Shank is the `ShankModule` interface. You create modules by implementing this interface:

```kotlin
object ApplicationModule : ShankModule {
    // Dependencies will be defined here
}
```

### Dependency Scopes

Shank offers three main scopes for dependencies:

1. **Singleton** (`single`) - A single instance for the entire application's lifecycle
2. **Factory** (`new`) - Creates a new instance each time
3. **Scoped** (`scoped`) - Instances are scoped to a specific context

## Defining Dependencies

### Singleton Dependencies

Use `single` for dependencies that should have a single instance throughout the application's lifecycle:

```kotlin
object ApplicationModule : ShankModule {
    // Clock is provided as a singleton
    val clock = single { -> Clock.systemUTC() }
    
    // Logger is a singleton with a typed interface
    val logger = single<Logger> { -> LoggerImpl() }
}
```

### Factory Dependencies (new instance each time)

Use `new` for dependencies that should be recreated each time they're requested:

```kotlin
object ApplicationModule : ShankModule {
    // Returns a new Instant every time it's called
    val now = new { -> Instant.now(clock()) }
}
```

### Scoped Dependencies

Use `scoped` for dependencies that are tied to a specific context or lifecycle:

```kotlin
object SessionModule : ShankModule {
    // User context scoped to a session
    val userContext = scoped { sessionId: String -> UserContext(sessionId) }
}
```

## Dependency Injection with Shank in Snitch

### Creating Modules

In a Snitch application, you typically organize dependencies by creating modules for different parts of your application:

```kotlin
// Database-related dependencies
object DBModule : ShankModule {
    val connection = single { ->
        Database.connect(
            "jdbc:postgresql://localhost:5432/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgres"
        )
    }
    
    val database = single { -> PostgresDatabase(connection()) }
}

// Repositories
object RepositoriesModule : ShankModule {
    val usersRepository = single<UsersRepository> { -> PostgresUsersRepository() }
    val postsRepository = single<PostsRepository> { -> PostgresPostsRepository() }
}

// Security-related dependencies
object SecurityModule : ShankModule {
    val keyPair = single { ->
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        keyPairGenerator.genKeyPair()
    }
    
    val privateKey = single { -> keyPair().private as RSAPrivateCrtKey }
    val publicKey = single { -> keyPair().public }
    val hasher = single<IPasswordHasher> { -> PasswordHasher(argon()) }
    val argon = single { -> Argon2Factory.create() }
    val jwt = single { -> JWTManager() }
}
```

### Using Dependencies in Handlers

Once you've defined your dependencies, you can use them in your Snitch handlers:

```kotlin
// User repository is injected
val usersRepository = RepositoriesModule.usersRepository()

val createUser by parsing<CreateUserRequest>() handling {
    // jwt manager is injected
    val jwt = SecurityModule.jwt()
    
    // passwordHasher is injected
    val hasher = SecurityModule.hasher()
    
    // Hash the password
    val hashedPassword = hasher.hash(body.password)
    
    // Save the user
    val user = usersRepository.createUser(body.username, hashedPassword)
    
    // Generate a token
    val token = jwt.createToken(user.id)
    
    // Return the response
    CreateUserResponse(user.id, token).created
}
```

## Advanced Shank Features

### Parameterized Dependencies

Shank supports dependencies with parameters:

```kotlin
object CacheModule : ShankModule {
    val cache = single { region: String -> Cache(region) }
}

// Usage
val userCache = CacheModule.cache("users")
val postCache = CacheModule.cache("posts")
```

### Type-bound Dependencies

You can bind an implementation to an interface:

```kotlin
object RepositoriesModule : ShankModule {
    // Bind PostgresUsersRepository to UsersRepository interface
    val usersRepository = single<UsersRepository> { -> PostgresUsersRepository() }
}

// Usage
val repo: UsersRepository = RepositoriesModule.usersRepository()
```

### Testing with Shank

Shank makes testing easier by allowing you to override dependencies:

```kotlin
@BeforeEach
fun setup() {
    // Override real implementation with mock
    RepositoriesModule.usersRepository.overrideFactory { -> mockUsersRepository }
}

@AfterEach
fun tearDown() {
    // Restore original implementation
    RepositoriesModule.usersRepository.restore()
    
    // Or reset all overrides
    resetShank()
}
```

## Integrating with Snitch Middleware

You can create middleware that injects dependencies:

```kotlin
val Router.withTransaction get() = decorateWith {
    val database = DBModule.postgresDatabase()
    database.transaction {
        next()
    }
}

// Usage
routes {
    withTransaction {
        POST("users") with body<CreateUserRequest>() isHandledBy createUser
    }
}
```

## Best Practices

1. **Organize by Module**: Group related dependencies in modules
2. **Interface-based Design**: Bind implementations to interfaces
3. **Single Responsibility**: Keep modules focused on a specific area
4. **Lazy Initialization**: Dependencies are only created when needed
5. **Testing**: Use overrides for test mocks

## Complete Example

Here's a complete example of using Shank with Snitch:

```kotlin
import snitch.dsl.*
import snitch.gson.GsonJsonParser
import snitch.shank.ShankModule
import snitch.shank.single

// Define modules
object AppModule : ShankModule {
    val logger = single { -> Logger() }
}

object UserModule : ShankModule {
    val userRepository = single { -> UserRepositoryImpl() }
    val userService = single { -> UserService(userRepository()) }
}

// Define handlers using dependencies
val getUserById by handling {
    val userService = UserModule.userService()
    val logger = AppModule.logger()
    
    logger.info("Getting user with ID: ${request[userId]}")
    
    val user = userService.getUser(request[userId])
    user?.ok ?: "User not found".notFound()
}

// Define parameters
val userId by path()

// Configure routes
fun main() {
    snitch(GsonJsonParser)
        .onRoutes {
            GET("users" / userId) isHandledBy getUserById
        }
        .start()
        .serveDocumenation()
}
```

## Performance Advantages

Shank's performance advantages come from several key design decisions:

1. **Zero reflection**: Unlike Spring or Guice which rely on runtime reflection, Shank uses pure Kotlin function references and type inference
2. **No proxy generation**: Dependencies are direct instances, not proxies, eliminating overhead
3. **No annotation processing**: No compile-time code generation or annotation scanning
4. **Lazy evaluation**: Dependencies are only instantiated when needed
5. **Minimal dependency graph traversal**: Optimized dependency resolution algorithm

These advantages result in:
- Faster application startup times
- Lower memory consumption
- Reduced CPU overhead
- Smaller deployment artifacts

## Summary

Shank provides the highest-performing, most type-safe dependency injection solution available for Kotlin applications, with unmatched integration with Snitch. By organizing dependencies into modules and leveraging Shank's powerful yet simple API, you can create maintainable, testable applications with minimal boilerplate and maximum performance.