---
slug: migrating-from-spring-to-snitch
title: "Migration Guide: From Spring Boot to Snitch"
authors: [snitch-team]
tags: [snitch, spring-boot, migration, kotlin]
---

# Migration Guide: From Spring Boot to Snitch

Many teams working with Kotlin find themselves using Spring Boot because it's the industry standard for Java applications. But as projects grow, they often encounter limitations with Spring's Java-centric approach when used with Kotlin. If you're considering migrating from Spring Boot to Snitch, this guide will help you navigate the transition with practical examples and a step-by-step approach.

<!-- truncate -->

## When to Consider Migration

Before diving into the how, let's address the when. Consider migrating from Spring Boot to Snitch if:

- You're primarily using Kotlin and want to leverage its features more effectively
- Your team struggles with Spring's annotation-heavy approach and "magic"
- You need better performance, particularly startup time and memory usage
- You want stronger compile-time guarantees and fewer runtime surprises
- Your documentation is constantly out of sync with your code

## Migration Strategy: Incremental Approach

The safest way to migrate is incrementally, especially for larger applications. Here's a proven strategy:

1. **Start with new endpoints** - Implement new functionality in Snitch
2. **Use the adapter pattern** - Create Snitch routes that delegate to existing Spring services
3. **Migrate endpoints in groups** - Move related endpoints together
4. **Replace cross-cutting concerns** - Migrate authentication, logging, etc.
5. **Eliminate Spring dependencies** - Finally remove Spring Boot completely

Let's look at how this works in practice.

## Converting REST Controllers to Snitch Routes

The most direct migration path is converting Spring controllers to Snitch routes.

### Spring Boot Controller:

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {
    
    @GetMapping
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<List<User>> {
        val users = userService.findAll(page, size)
        return ResponseEntity.ok(users)
    }
    
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<User> {
        return userService.findById(id)
            .map { ResponseEntity.ok(it) }
            .orElse(ResponseEntity.notFound().build())
    }
    
    @PostMapping
    fun createUser(@RequestBody @Valid user: CreateUserRequest): ResponseEntity<User> {
        val created = userService.create(user)
        return ResponseEntity
            .created(URI.create("/api/users/${created.id}"))
            .body(created)
    }
}
```

### Equivalent Snitch Routes:

```kotlin
// Define parameters
val page by query(ofNonNegativeInt, default = 0)
val size by query(ofIntRange(1, 100), default = 20)
val userId by path(ofLong)
val createUserRequest by body<CreateUserRequest>()

// Define routes
val userRoutes = routes {
    "api" / "users" / {
        GET() withQueries(page, size) isHandledBy {
            val users = userService.findAll(request[page], request[size])
            users.ok
        }
        
        GET(userId) isHandledBy {
            userService.findById(request[userId])
                ?.ok
                ?: "User not found".notFound()
        }
        
        POST() with createUserRequest isHandledBy {
            val user = userService.create(request[createUserRequest])
            user.created
        }
    }
}
```

## Migrating Spring Security to Snitch Conditions

Authentication and authorization are critical concerns. Here's how to migrate from Spring Security:

### Spring Boot Security:

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig : WebSecurityConfigurerAdapter() {
    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            .antMatchers("/api/public/**").permitAll()
            .antMatchers("/api/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer().jwt()
    }
}

@RestController
@RequestMapping("/api/admin")
class AdminController {
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    fun getDashboard(): ResponseEntity<Dashboard> {
        // Implementation
    }
}
```

### Equivalent Snitch Implementation:

```kotlin
// Authentication middleware
val Router.authenticated get() = decorateWith {
    val auth = request.headerParams("Authorization").firstOrNull()
    if (auth == null || !auth.startsWith("Bearer ")) {
        return@decorateWith "Unauthorized".unauthorized()
    }
    
    val token = auth.substring(7)
    val claims = jwtVerifier.verify(token)
    
    // Store in request attributes for later use
    request.attributes["user"] = User.fromClaims(claims)
    next()
}

// Role-based conditions
val hasAdminRole = condition("hasAdminRole") {
    val user = request.attributes["user"] as? User
    if (user?.roles?.contains("ADMIN") == true) {
        ConditionResult.Successful
    } else {
        ConditionResult.Failed("Admin role required".forbidden())
    }
}

// Routes with authentication and authorization
val adminRoutes = routes {
    authenticated {
        "api" / "admin" / {
            "dashboard" / {
                GET() onlyIf hasAdminRole isHandledBy {
                    // Implementation
                    dashboardService.getDashboard().ok
                }
            }
        }
    }
    
    "api" / "public" / {
        // Public routes don't need authentication
        GET("health") isHandledBy { "OK".ok }
    }
}
```

## Migrating Validation Logic

Spring's validation relies heavily on Bean Validation annotations. Here's how to migrate to Snitch's validator system:

### Spring Boot Validation:

```kotlin
data class CreateUserRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
    
    @field:Email(message = "Valid email is required")
    @field:NotBlank(message = "Email is required")
    val email: String,
    
    @field:Min(value = 18, message = "Age must be at least 18")
    val age: Int
)

@PostMapping
fun createUser(@RequestBody @Valid request: CreateUserRequest): ResponseEntity<User> {
    // Implementation
}
```

### Equivalent Snitch Validation:

```kotlin
data class CreateUserRequest(
    val name: String,
    val email: String,
    val age: Int
)

val ofCreateUserRequest = bodyValidator<CreateUserRequest>("valid user request") { request ->
    when {
        request.name.isBlank() -> throw ValidationException("Name is required")
        !isValidEmail(request.email) -> throw ValidationException("Valid email is required")
        request.age < 18 -> throw ValidationException("Age must be at least 18")
        else -> request
    }
}

val createUserReq by body(ofCreateUserRequest)

POST("users") with createUserReq isHandledBy {
    // Implementation with validated request
    val validatedRequest = request[createUserReq]
    userService.create(validatedRequest).created
}
```

## Migrating Exception Handling

Spring's `@ExceptionHandler` and `@ControllerAdvice` can be migrated to Snitch's exception handling:

### Spring Boot Exception Handling:

```kotlin
@ControllerAdvice
class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFound(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(ex.message ?: "Resource not found"))
    }
    
    @ExceptionHandler(ValidationException::class)
    fun handleValidation(ex: ValidationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(ex.message ?: "Validation failed"))
    }
}
```

### Equivalent Snitch Exception Handling:

```kotlin
snitch(GsonJsonParser)
    .onRoutes(routes)
    .handleException(ResourceNotFoundException::class) { ex ->
        ErrorResponse(ex.message ?: "Resource not found").notFound()
    }
    .handleException(ValidationException::class) { ex ->
        ErrorResponse(ex.message ?: "Validation failed").badRequest()
    }
    .start()
```

## Adapting Spring Services

During migration, you'll likely keep using Spring services for a while. Here's how to bridge the gap:

```kotlin
// Existing Spring service
@Service
class UserServiceImpl(private val userRepository: UserRepository) : UserService {
    // Implementation
}

// Snitch route using Spring service
val springUserService = springContext.getBean(UserService::class.java)

val userRoutes = routes {
    "api" / "users" / {
        GET(userId) isHandledBy {
            // Use Spring service from Snitch handler
            springUserService.findById(request[userId])
                ?.ok
                ?: "User not found".notFound()
        }
    }
}
```

## Configuration Migration

Replace Spring's property-based configuration with Snitch's configuration:

### Spring Boot Configuration:

```kotlin
@Configuration
@ConfigurationProperties(prefix = "app")
data class AppConfig(
    var apiKey: String = "",
    var maxConnections: Int = 10,
    var timeoutSeconds: Int = 30
)

@RestController
class ConfiguredController(private val appConfig: AppConfig) {
    // Use configuration
}
```

### Equivalent Snitch Configuration:

```kotlin
data class AppConfig(
    val apiKey: String,
    val maxConnections: Int,
    val timeoutSeconds: Int
)

val config = ConfigFactory.load()
val appConfig = AppConfig(
    apiKey = config.getString("app.apiKey"),
    maxConnections = config.getInt("app.maxConnections"),
    timeoutSeconds = config.getInt("app.timeoutSeconds")
)

val configuredRoutes = routes {
    // Use configuration
}
```

## Testing Approach

Migrate from Spring Boot testing to Snitch's testing DSL:

### Spring Boot Test:

```kotlin
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    
    @Autowired
    private lateinit var mockMvc: MockMvc
    
    @MockBean
    private lateinit var userService: UserService
    
    @Test
    fun `should return user when found`() {
        // Given
        val user = User(1, "Test User", "test@example.com")
        whenever(userService.findById(1)).thenReturn(Optional.of(user))
        
        // When/Then
        mockMvc.perform(get("/api/users/1"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.name").value("Test User"))
    }
}
```

### Equivalent Snitch Test:

```kotlin
class UserRoutesTest : SnitchTest(/* configure app */) {
    
    private val userService = mockk<UserService>()
    
    init {
        // Wire up the mock service
        registerRoutes(createUserRoutes(userService))
    }
    
    @Test
    fun `should return user when found`() {
        // Given
        val user = User(1, "Test User", "test@example.com")
        every { userService.findById(1) } returns user
        
        // When/Then
        GET("/api/users/1")
            .expectCode(200)
            .expectJson {
                it.path("name").asText() shouldBe "Test User"
            }
    }
}
```

## Conclusion: Embracing Idioms Over Conventions

Migrating from Spring Boot to Snitch is fundamentally about shifting from a convention-based approach to an idiomatic Kotlin approach. While Spring Boot hides complexity through conventions, Snitch makes behavior explicit through an expressive DSL.

The migration process requires rethinking how your application is structured, but the rewards are substantial:
- More readable, maintainable code
- Stronger compile-time guarantees
- Better performance characteristics
- Automated documentation that stays in sync
- A more natural Kotlin development experience

By following an incremental approach and leveraging the patterns outlined above, you can successfully transition even large applications from Spring Boot to Snitch, unlocking the full potential of Kotlin for your web services.