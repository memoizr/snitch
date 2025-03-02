# Advanced Shank Design Patterns

This document explores powerful design patterns and techniques for using Shank effectively in your applications. While Shank is intuitive and straightforward by design, these patterns can help you tackle more complex scenarios with elegance and maintainability.

## Table of Contents

1. [Module Organization Strategies](#module-organization-strategies)
2. [Dependency Layering](#dependency-layering)
3. [Testing Patterns](#testing-patterns)
4. [Domain-Driven Design with Shank](#domain-driven-design-with-shank)
5. [Feature Toggling](#feature-toggling)
6. [Lifecycle Management](#lifecycle-management)
7. [Configuration Injection](#configuration-injection)
8. [Conditional Dependencies](#conditional-dependencies)

## Module Organization Strategies

### Domain Module Pattern

Organize your modules by domain context rather than technical layers:

```kotlin
// Instead of "RepositoryModule", "ServiceModule", etc.
object UserDomainModule : ShankModule {
    // Domain-specific repositories
    val userRepository = single<UserRepository> { -> UserRepositoryImpl() }
    
    // Domain-specific services
    val userService = single<UserService> { -> UserServiceImpl(userRepository()) }
    
    // Domain-specific validation
    val userValidator = single { -> UserValidator() }
}

object PaymentDomainModule : ShankModule {
    val paymentRepository = single<PaymentRepository> { -> PaymentRepositoryImpl() }
    val paymentService = single<PaymentService> { -> PaymentServiceImpl(paymentRepository()) }
    val paymentProcessor = single { -> PaymentProcessor() }
}
```

This approach aligns better with domain-driven design principles and improves cohesion, making each module more focused and self-contained.

### Feature Module Pattern

For larger applications, organize modules by features that cut across domain boundaries:

```kotlin
object AuthenticationModule : ShankModule {
    val passwordEncoder = single { -> BCryptPasswordEncoder() }
    val tokenService = single { -> JwtTokenService() }
    val authenticationService = single { -> 
        AuthenticationServiceImpl(
            UserDomainModule.userRepository(),
            passwordEncoder(),
            tokenService()
        )
    }
}

object CheckoutModule : ShankModule {
    val checkoutService = single { -> 
        CheckoutServiceImpl(
            UserDomainModule.userService(),
            PaymentDomainModule.paymentService(),
            ShippingDomainModule.shippingService()
        ) 
    }
}
```

This pattern maximizes code organization around user-facing features, making it clear which dependencies belong to which features.

## Dependency Layering

Shank allows you to create clear architectural boundaries while maintaining explicit dependency relationships:

```kotlin
// Infrastructure Layer
object InfrastructureModule : ShankModule {
    val database = single { -> PostgresDatabase(connectionString()) }
    val httpClient = single { -> OkHttpClient() }
    val cache = single { -> RedisCache(redisConfig()) }
}

// Repository Layer
object RepositoryModule : ShankModule {
    val userRepository = single<UserRepository> { -> 
        UserRepositoryImpl(InfrastructureModule.database())
    }
}

// Service Layer
object ServiceModule : ShankModule {
    val userService = single<UserService> { -> 
        UserServiceImpl(RepositoryModule.userRepository())
    }
}

// Application Layer
object ApplicationModule : ShankModule {
    val userController = single { -> 
        UserController(ServiceModule.userService())
    }
}
```

This approach enforces the dependency rule (dependencies only point inward) while maintaining Shank's explicit dependency tracing.

## Testing Patterns

### Test Double Pattern

Create specialized modules for testing that override production dependencies:

```kotlin
// Production module
object PaymentModule : ShankModule {
    val paymentGateway = single<PaymentGateway> { -> StripePaymentGateway() }
    val paymentService = single { -> PaymentServiceImpl(paymentGateway()) }
}

// Test module
object TestPaymentModule : ShankModule {
    val mockPaymentGateway = single<PaymentGateway> { -> MockPaymentGateway() }
    
    fun setupForTesting() {
        // Override the real implementation with our mock
        PaymentModule.paymentGateway.overrideFactory { -> mockPaymentGateway() }
    }
    
    fun tearDown() {
        // Restore the original implementation
        PaymentModule.paymentGateway.restore()
    }
}
```

Then in your tests:

```kotlin
class PaymentServiceTest {
    @BeforeEach
    fun setup() {
        TestPaymentModule.setupForTesting()
    }
    
    @AfterEach
    fun tearDown() {
        TestPaymentModule.tearDown()
    }
    
    @Test
    fun `test payment processing`() {
        // This will use the mock gateway
        val paymentService = PaymentModule.paymentService()
        
        // Configure the mock
        val mockGateway = TestPaymentModule.mockPaymentGateway()
        mockGateway.setupToReturnSuccessfulPayment()
        
        // Execute the test
        val result = paymentService.processPayment(100.0, "USD")
        
        // Verify the result
        assertEquals(PaymentStatus.SUCCESS, result.status)
    }
}
```

This approach provides fine-grained control over which dependencies are mocked in tests while preserving the rest of the dependency tree.

### Component Testing Pattern

For integration/component testing, create specialized test configurations:

```kotlin
object TestDatabaseModule : ShankModule {
    val inMemoryDatabase = single { -> H2Database() }
    
    fun setupTestEnvironment() {
        // Override production database with in-memory version
        InfrastructureModule.database.overrideFactory { -> inMemoryDatabase() }
    }
}

object IntegrationTestModule : ShankModule {
    val testHelper = single { -> IntegrationTestHelper() }
    
    fun prepareIntegrationTest() {
        TestDatabaseModule.setupTestEnvironment()
        // Additional test setup...
    }
}
```

## Domain-Driven Design with Shank

Shank works beautifully with Domain-Driven Design (DDD) principles:

```kotlin
// Domain layer - contains pure domain logic
object DomainModule : ShankModule {
    val userFactory = single { -> UserFactory() }
    val paymentPolicy = single { -> BusinessPaymentPolicy() }
}

// Application layer - orchestrates use cases
object ApplicationModule : ShankModule {
    val createUserUseCase = single { -> 
        CreateUserUseCase(
            DomainModule.userFactory(),
            RepositoryModule.userRepository()
        )
    }
    
    val processPaymentUseCase = single { -> 
        ProcessPaymentUseCase(
            DomainModule.paymentPolicy(),
            RepositoryModule.paymentRepository()
        )
    }
}

// Infrastructure layer - provides implementations
object InfrastructureModule : ShankModule {
    val userRepositoryImpl = single<UserRepository> { -> 
        PostgresUserRepository(DatabaseModule.database())
    }
}
```

This structure helps maintain a clean domain model while keeping infrastructure concerns separate. The domain module doesn't depend on any external modules, preserving the domain's purity.

## Feature Toggling

Implement feature toggling with Shank:

```kotlin
object FeatureToggleModule : ShankModule {
    val featureManager = single { -> FeatureManager() }
    
    // Define toggleable implementations
    val paymentProcessor = single<PaymentProcessor> { ->
        val featureManager = featureManager()
        if (featureManager.isEnabled("new-payment-processor")) {
            NewPaymentProcessor()
        } else {
            LegacyPaymentProcessor()
        }
    }
    
    // Conditionally provide implementations
    val betaFeatures = single { -> 
        val featureManager = featureManager()
        if (featureManager.isEnabled("beta-features")) {
            BetaFeaturesImpl() 
        } else {
            EmptyBetaFeatures()
        }
    }
}
```

This pattern allows you to toggle features at runtime while maintaining Shank's type safety and explicit dependency structure.

## Lifecycle Management

For dependencies that need initialization or cleanup:

```kotlin
object ResourceModule : ShankModule {
    val databaseClient = single { -> 
        DatabaseClient().also { client ->
            // Register shutdown hook
            Runtime.getRuntime().addShutdownHook(Thread {
                client.close()
            })
        }
    }
    
    // Auto-closeable resources
    val httpClient = single { -> 
        OkHttpClient().also { client ->
            registerForCleanup(client)
        }
    }
    
    // Track resources that need to be closed
    private val managedResources = mutableListOf<AutoCloseable>()
    
    private fun registerForCleanup(resource: AutoCloseable) {
        managedResources.add(resource)
    }
    
    fun closeAll() {
        managedResources.forEach { it.close() }
        managedResources.clear()
    }
}
```

To use in your application:

```kotlin
fun main() {
    try {
        // Use your application...
        val app = startApplication()
        app.waitForShutdown()
    } finally {
        ResourceModule.closeAll()
    }
}
```

## Configuration Injection

Manage configuration values through Shank modules:

```kotlin
object ConfigModule : ShankModule {
    val config = single { -> 
        ConfigFactory.load() 
    }
    
    val databaseConfig = single { -> 
        val config = config()
        DatabaseConfig(
            url = config.getString("db.url"),
            username = config.getString("db.username"),
            password = config.getString("db.password")
        )
    }
    
    val apiConfig = single { -> 
        val config = config()
        ApiConfig(
            baseUrl = config.getString("api.baseUrl"),
            timeout = config.getDuration("api.timeout")
        )
    }
}

// Usage in other modules
object DatabaseModule : ShankModule {
    val database = single { -> 
        val config = ConfigModule.databaseConfig()
        Database.connect(
            url = config.url,
            username = config.username,
            password = config.password
        )
    }
}
```

This pattern centralizes configuration while maintaining type safety.

## Conditional Dependencies

Provide different implementations based on environment or other conditions:

```kotlin
object NotificationModule : ShankModule {
    val emailSender = single<EmailSender> { ->
        when (Environment.current) {
            Environment.PRODUCTION -> SmtpEmailSender(ConfigModule.emailConfig())
            Environment.STAGING -> SmtpEmailSender(ConfigModule.emailConfig())
            Environment.DEVELOPMENT -> ConsoleEmailSender()
            Environment.TEST -> NoOpEmailSender()
        }
    }
    
    val pushNotificationService = single<PushNotificationService> { ->
        if (Environment.isProduction()) {
            FirebasePushService(ConfigModule.firebaseConfig())
        } else {
            LoggingPushService()
        }
    }
}
```

This approach provides environment-specific implementations while maintaining the same dependency interface.

## Summary

These patterns demonstrate Shank's flexibility and power in handling complex dependency scenarios while maintaining its core advantages of explicitness, type safety, and performance. By applying these patterns, you can build well-structured, maintainable applications that scale with your needs.

Shank's design philosophy emphasizes clarity and explicitness, making it not just a technical tool but a design aid that helps you think more clearly about your application's structure and dependencies.