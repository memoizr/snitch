# Kofix: Property-Based Testing and Random Data Generation for Kotlin

Kofix is a powerful property-based testing and object generation framework for Kotlin applications. It helps you create realistic test data without manually constructing complex object hierarchies, allowing you to focus on writing meaningful tests instead of boilerplate setup code.

## Features

- **Random test data generation** for any Kotlin class, interface, or data structure
- **Customizable object factories** for fine-grained control over test data
- **Property delegation syntax** for easy and concise usage
- **Seed control** for reproducible tests
- **Support for complex types** including:
  - Generic classes and interfaces
  - Nested collections
  - Value classes
  - Nullable properties
  - Sealed classes
  - Abstract classes
  - Java interoperability

## Installation

Add the Kofix dependency to your `build.gradle.kts` file:

```kotlin
dependencies {
    testImplementation("io.github.memoizr:snitch-kofix:1.0.0")
}
```

## Basic Usage

### Creating Random Objects

The most common way to use Kofix is with the property delegate syntax:

```kotlin
class UserServiceTest {
    // Generate random instances
    val user by aRandom<User>()
    val posts by aRandomListOf<Post>()
    
    @Test
    fun `test user creation`() {
        // Each test run has different random values
        userService.createUser(user)
        
        val createdUser = userService.getUser(user.id)
        assertEquals(user.name, createdUser.name)
    }
}
```

### Customizing Generated Objects

You can customize generated objects to fit your test scenarios:

```kotlin
// Customize individual instances
val activeUser by aRandom<User> { 
    copy(isActive = true, createdAt = Instant.now()) 
}

// Customize lists
val recentPosts by aRandomListOf<Post> { 
    map { it.copy(createdAt = Instant.now().minus(7, ChronoUnit.DAYS)) }
}
```

### Global Type Customization

For consistent customization across tests, register global type customizers:

```kotlin
// In a test base class or setup method
customize<Email> { Email("${a<String>()}@example.com") }
customize<UserId> { UserId(UUID.randomUUID().toString()) }
customize<Instant> { Instant.now() }
```

### Control Collection Sizes

Define collection size constraints as needed:

```kotlin
// Fixed size list (exactly 5 elements)
val smallList by aRandomListOf<String>(size = 5)

// Bounded size (between 10 and 20 elements)
val mediumList by aRandomListOf<String>(minSize = 10, maxSize = 20)
```

### Functional API

For one-off object creation, use the functional API:

```kotlin
val randomUser = a<User>()
val randomInts = aList<Int>(size = 10)
```

## Advanced Features

### Seed Control for Reproducibility

Control randomization with a seed for reproducible tests:

```kotlin
// Set a fixed seed for reproducible tests
Seed.seed = 12345L
val user by aRandom<User>() // Always the same user with fixed seed

// Use testing mode for random but consistent tests
@Before
fun setup() {
    Seed.testing = true // Different on each run but consistent within a test
}
```

### Complex Generic Types

Kofix fully supports generic types:

```kotlin
// Complex generic type
val pairStringListInt by aRandom<Pair<String, List<Int>>>()
val nestedMap by aRandom<Map<String, List<Pair<Int, User>>>>()
```

### Working with Interfaces and Abstract Classes

Kofix creates implementations of interfaces and abstract classes:

```kotlin
// Random implementation of repository interface
val repository by aRandom<UserRepository>()

// Now you can use the interface methods
val users = repository.findAllUsers()
```

## Best Practices

### Test Structure

Structure your tests to take advantage of property-based testing:

```kotlin
class UserServiceTest {
    // Define your random fixtures
    val user by aRandom<User>()
    val admin by aRandom<User> { copy(role = Role.ADMIN) }
    
    // Initialize dependencies with your fixtures
    @BeforeEach
    fun setup() {
        userRepository.save(user)
        userRepository.save(admin)
    }
    
    @Test
    fun `regular users cannot delete other users`() {
        val result = userService.deleteUser(user, admin.id)
        assertEquals(AccessResult.DENIED, result)
    }
    
    @Test
    fun `admin users can delete other users`() {
        val result = userService.deleteUser(admin, user.id)
        assertEquals(AccessResult.GRANTED, result)
    }
}
```

### Using with Snitch Testing Framework

Kofix integrates seamlessly with Snitch's testing framework:

```kotlin
class ApiTest : SnitchTest({ Application.setup(it) }) {
    // Random test data
    val user by aRandom<User>()
    val createUserRequest by aRandom<CreateUserRequest>()
    
    @Test
    fun `can create a user`() {
        POST("/users")
            .withBody(createUserRequest)
            .expectCode(201)
            .expectJsonPath("$.id", notNull)
    }
}
```

## How It Works

Kofix uses Kotlin's reflection capabilities to analyze the type structure of your classes and interfaces, then generates random values for all properties. It handles complex types by recursively creating objects for nested properties, and maintains object identity within a test case.

The library prioritizes:

1. **Simplicity** - The API is designed to be intuitive and reduce boilerplate
2. **Flexibility** - Customization is available at both instance and type level
3. **Reliability** - Generated objects adhere to the constraints of the Kotlin type system
4. **Performance** - Object caching and efficient creation algorithms

## License

Kofix is available under the same license as the Snitch framework.