# Property-Based Testing with Kofix

This tutorial introduces property-based testing in Snitch applications using the Kofix library. You'll learn how to use Kofix to generate test data, write more robust tests, and reduce test boilerplate.

## What is Property-Based Testing?

Property-based testing is an approach where instead of writing tests with specific input values, you define properties that should hold true for all possible inputs. The testing framework then generates random inputs to verify these properties.

This approach has several advantages:
- Discovers edge cases you might not think of
- Reduces test maintenance as requirements change
- Provides better test coverage with less code
- Helps identify subtle bugs that appear only with specific inputs

## Getting Started with Kofix

### Installation

First, add the Kofix dependency to your project:

```kotlin
dependencies {
    testImplementation("io.github.memoizr:snitch-kofix:1.0.0")
}
```

### Creating Your First Property-Based Test

Let's start with a simple example - testing a user registration service:

```kotlin
class UserServiceTest {
    // Create random test data using property delegation
    val user by aRandom<User>()
    val email by aRandom<Email>()
    
    @Test
    fun `registered users can be retrieved by email`() {
        // Act
        userService.register(user)
        
        // Assert
        val retrievedUser = userService.findByEmail(user.email)
        assertEquals(user.id, retrievedUser?.id)
        assertEquals(user.name, retrievedUser?.name)
    }
}
```

In this example, Kofix automatically generates random `User` objects with all required properties filled with sensible random values.

## Core Concepts

### Random Object Generation

Kofix can generate random instances of any class:

```kotlin
// Simple types
val randomString by aRandom<String>()
val randomInt by aRandom<Int>()
val randomInstant by aRandom<Instant>()

// Domain objects
val user by aRandom<User>()
val product by aRandom<Product>()

// Even complex generic types
val listOfMaps by aRandom<List<Map<String, User>>>()
```

### Customizing Generated Objects

Often you need to customize the generated objects for specific test scenarios:

```kotlin
// Customize individual instances
val activeUser by aRandom<User> { 
    copy(status = UserStatus.ACTIVE, verifiedEmail = true) 
}

// Customize with dependencies between objects
val order by aRandom<Order>()
val orderItem by aRandom<OrderItem> {
    copy(orderId = order.id)
}
```

### Collections of Random Objects

For testing with collections:

```kotlin
// Random lists with default size (1-10 elements)
val users by aRandomListOf<User>() 

// Fixed size
val fiveProducts by aRandomListOf<Product>(size = 5)

// Size range
val orders by aRandomListOf<Order>(minSize = 2, maxSize = 10)

// Customized lists
val activeUsers by aRandomListOf<User> {
    map { it.copy(status = UserStatus.ACTIVE) }
}
```

## Integration with Snitch Tests

Kofix is particularly powerful when combined with Snitch's testing framework. Let's see how to use it in API tests:

```kotlin
class UserApiTest : SnitchTest({ Application.setup(it) }) {
    // Test data
    val user by aRandom<User>()
    val createUserRequest by aRandom<CreateUserRequest>()
    val updateUserRequest by aRandom<UpdateUserRequest>()
    
    // JWT tokens for authentication
    lateinit var userToken: String
    
    @BeforeEach
    fun setup() {
        // Use the random user to create a real user in the system
        userService.createUser(user)
        userToken = jwtService.createToken(user.id)
    }
    
    @Test
    fun `authenticated users can update their profile`() {
        // Send a PUT request with random data
        PUT("/users/${user.id}")
            .withHeaders(mapOf("Authorization" to "Bearer $userToken"))
            .withBody(updateUserRequest)
            .expectCode(200)
        
        // Verify the update was applied
        GET("/users/${user.id}")
            .withHeaders(mapOf("Authorization" to "Bearer $userToken"))
            .expectCode(200)
            .expectJsonPath("$.name", updateUserRequest.name)
            .expectJsonPath("$.email", updateUserRequest.email)
    }
}
```

Here's a more complete example based on the `PostsRoutesTest` from the Snitch example project:

```kotlin
class PostsRoutesTest : BaseTest() {
    // Random data for our tests
    val createPostRequest by aRandom<CreatePostRequest>()
    val updatePostRequest by aRandom<UpdatePostRequest>()

    // Different users with different roles
    val otherUser by aRandom<User>()
    val user by aRandom<User>()
    val admin by aRandom<User> { copy(role = Role.ADMIN) }
    
    // Posts owned by different users
    val post by aRandom<Post> { 
        copy(creator = UserView(user.id, user.name), createdAt = now()) 
    }
    val postByOtherUser by aRandom<Post> { 
        copy(creator = UserView(otherUser.id, otherUser.name), createdAt = now()) 
    }

    // Authentication tokens
    lateinit var userToken: String
    lateinit var adminToken: String

    @BeforeEach
    fun setup() {
        // Setup test environment with our random users
        userToken = user.create().let { jwt().newToken(JWTClaims(user.id, Role.USER)) }
        adminToken = admin.create().let { jwt().newToken(JWTClaims(admin.id, Role.ADMIN)) }
        otherUser.create()
    }

    @Test
    fun `a logged in user can create a post then view it`() {
        // Create a post with random data
        POST("/users/${user.id.value}/posts")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .withBody(createPostRequest)
            .expectCode(201)

        // Verify we can view the created post
        GET("/users/${user.id.value}/posts")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .expectCode(200)
            .expectJsonPath("$.posts[0].title", createPostRequest.title)
            .expectJsonPath("$.posts[0].content", createPostRequest.content)
    }
    
    @Test
    fun `a user cannot post on another user's behalf`() {
        POST("/users/${otherUser.id.value}/posts")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .withBody(createPostRequest)
            .expectCode(403)
    }
}
```

## Advanced Features

### Global Type Customization

For consistent data generation across tests, customize types globally:

```kotlin
// In your BaseTest class
init {
    // Custom email format
    customize<Email> { Email("${randomString()}@example.com") }
    
    // Current timestamp for all tests
    customize<Instant> { clock.now() }
    
    // UUIDs for IDs
    customize<UserId> { UserId(UUID.randomUUID().toString()) }
}
```

The example project does this in `BaseTest.kt`:

```kotlin
abstract class BaseTest : SnitchTest({ Application.setup(it) }) {
    init {
        connection()
        customize<Email> { Email("${randomString()}@${randomString()}.com") }
    }
    
    fun randomString(n: Int = 1, m: Int = 5): String {
        val length = Random.nextInt(n, m + 1)
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(length) { chars.random() }.joinToString("")
    }
    
    // ...
}
```

### Seed Control for Reproducible Tests

Control randomness with seeds for reproducible tests:

```kotlin
// Fixed seed for deterministic tests
@BeforeEach
fun setupSeed() {
    Seed.seed = 12345L // Same random values every time
}

// Test mode for consistent random values within a test
@BeforeEach
fun setupTestMode() {
    Seed.testing = true // Different between test runs but consistent within a test
}
```

### Testing with Interfaces and Abstract Classes

Kofix can even generate implementations of interfaces and abstract classes:

```kotlin
// Generates a mock implementation of the interface
val repository by aRandom<UserRepository>()

@Test
fun `test with mock repository`() {
    // The mock implementation will return random data
    val users = repository.findAllUsers()
    assertTrue(users.isNotEmpty())
}
```

## Best Practices

### 1. Combine Property-Based and Example-Based Testing

Use property-based testing for broad verification and example-based testing for specific cases:

```kotlin
@Test
fun `email validation rejects invalid formats`() {
    // Property-based: generate many random invalid emails
    repeat(100) {
        val invalidEmail = "${a<String>()}${a<String>()}"
        assertFalse(emailValidator.isValid(invalidEmail))
    }
    
    // Example-based: specific cases that must work
    assertTrue(emailValidator.isValid("user@example.com"))
    assertFalse(emailValidator.isValid("user@"))
}
```

### 2. Use Custom Generators for Domain Constraints

Create custom generators for domain-specific constraints:

```kotlin
// In your BaseTest class
init {
    // Valid product codes must follow a pattern
    customize<ProductCode> { 
        val category = listOf("HW", "SW", "SRV").random()
        val number = (1000..9999).random()
        ProductCode("$category-$number") 
    }
}
```

### 3. Structure Test Data for Readability

Structure your test data declarations for better readability:

```kotlin
class OrderTest {
    // Group related test data together
    object TestUsers {
        val customer by aRandom<User>()
        val admin by aRandom<User> { copy(role = Role.ADMIN) }
    }
    
    object TestProducts {
        val inStock by aRandom<Product> { copy(stockQuantity = 100) }
        val outOfStock by aRandom<Product> { copy(stockQuantity = 0) }
    }
    
    // Use the structured data in tests
    @Test
    fun `customers cannot order out-of-stock products`() {
        val result = orderService.placeOrder(
            TestUsers.customer, 
            TestProducts.outOfStock, 
            quantity = 1
        )
        
        assertEquals(OrderResult.OUT_OF_STOCK, result)
    }
}
```

### 4. Test Boundary Cases Explicitly

Even with property-based testing, explicitly test boundary cases:

```kotlin
@Test
fun `boundary cases are handled correctly`() {
    // Zero quantity
    val zeroQuantityOrder by aRandom<Order> { copy(quantity = 0) }
    assertFalse(orderValidator.isValid(zeroQuantityOrder))
    
    // Maximum quantity
    val maxQuantityOrder by aRandom<Order> { copy(quantity = MAX_QUANTITY) }
    assertTrue(orderValidator.isValid(maxQuantityOrder))
    
    // Quantity just above maximum
    val tooLargeOrder by aRandom<Order> { copy(quantity = MAX_QUANTITY + 1) }
    assertFalse(orderValidator.isValid(tooLargeOrder))
}
```

## Common Pitfalls and Solutions

### Slow Tests Due to Large Object Graphs

**Problem**: Tests are slow because Kofix generates large object graphs.

**Solution**: Limit collection sizes and customize complex objects:

```kotlin
// Limit collection sizes
val smallList by aRandomListOf<ComplexObject>(maxSize = 3)

// Simplify complex objects
customize<ComplexObject> {
    // Create a simplified version with only essential properties
    ComplexObject(
        id = a<Id>(),
        name = a<String>(),
        // Use empty collections instead of generating them
        references = emptyList()
    )
}
```

### Non-Deterministic Test Failures

**Problem**: Tests sometimes fail due to randomness.

**Solution**: Use seed control and properly constrain your generators:

```kotlin
@BeforeEach
fun setupSeed() {
    // When a test fails, log the seed value and use it here to reproduce
    Seed.seed = System.getProperty("test.seed")?.toLongOrNull() 
        ?: Random.nextLong().also { println("Test seed: $it") }
}
```

### Unnecessary Object Creation

**Problem**: Creating too many objects when only a few properties are needed.

**Solution**: Use domain-specific generators or customize at use:

```kotlin
// Instead of a full user with all properties
val simpleUser by aRandom<User> {
    // Keep only what you need for the test
    copy(
        email = Email("test@example.com"),
        password = null,
        profile = null,
        settings = emptyMap(),
        // Clear other complex properties
        address = null
    )
}
```

## Conclusion

Property-based testing with Kofix helps you write more robust tests with less code. By generating random but valid test data, you can discover edge cases and ensure your code works correctly across a wide range of inputs.

Kofix integrates seamlessly with Snitch's testing framework, making it easy to test your API endpoints with realistic, randomly generated data. This approach is particularly valuable for testing business logic, validation rules, and API endpoints.