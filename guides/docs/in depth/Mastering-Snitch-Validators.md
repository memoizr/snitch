# Validators

Validators are a core feature of Snitch that ensure your HTTP inputs are properly validated, transformed, and documented. This guide will walk you through everything you need to know about validators, from basic usage to advanced customization.

## Introduction to Validators

In HTTP applications, inputs from requests (path parameters, query parameters, headers, body) are always strings or collections of strings. However, your business logic typically requires strongly-typed values with guaranteed validity. Validators are the bridge that transforms these raw inputs into safe, typed values.

At their core, validators in Snitch serve three main purposes:

1. **Validation**: Ensuring inputs meet specific criteria
2. **Transformation**: Converting strings to appropriate target types
3. **Documentation**: Providing clear descriptions for API documentation

The `Validator<T, R>` interface is defined with two type parameters:
- `T`: The input type (usually `String`)
- `R`: The output type (the type you want to work with in your code)

And three main components:
- `regex`: A regular expression pattern for basic string validation
- `description`: A human-readable description for documentation
- `parse`: A function that transforms validated input into the output type

## Built-in Validators

Snitch comes with a comprehensive set of built-in validators for common use cases:

### Numeric Validators

```kotlin
// Integer validators
val ofInt: Validator<Int, Int>
val ofNonNegativeInt: Validator<Int, Int>
val ofPositiveInt: Validator<Int, Int>
fun ofIntRange(min: Int, max: Int): Validator<Int, Int>

// Decimal validators
val ofDouble: Validator<Double, Double>
fun ofDoubleRange(min: Double, max: Double): Validator<Double, Double>
```

### String Validators

```kotlin
val ofNonEmptyString: Validator<String, String>
val ofNonEmptySingleLineString: Validator<String, String>
fun ofStringLength(minLength: Int, maxLength: Int): Validator<String, String>
val ofAlphanumeric: Validator<String, String>
fun ofRegexPattern(pattern: String, description: String): Validator<String, String>
```

### Special Format Validators

```kotlin
val ofEmail: Validator<String, String>
val ofUrl: Validator<String, URI>
val ofIpv4: Validator<String, String>
val ofPhoneNumber: Validator<String, String>
val ofJson: Validator<String, String>
```

### Date/Time Validators

```kotlin
val ofDate: Validator<String, LocalDate>
val ofDateTime: Validator<String, LocalDateTime>
fun ofDateFormat(format: String): Validator<String, LocalDate>
```

### Collection Validators

```kotlin
val ofStringSet: Validator<String, Set<String>>
val ofNonEmptyStringSet: Validator<String, Set<String>>
```

### Boolean Validators

```kotlin
val ofBoolean: Validator<Boolean, Boolean>  // Handles true/false, yes/no, 1/0
```

### ID Validators

```kotlin
val ofUuid: Validator<String, UUID>
```

### Enum Validators

```kotlin
inline fun <reified E : Enum<*>> ofEnum(): Validator<String, E>
inline fun <reified E : Enum<*>> ofRepeatableEnum(): Validator<String, Collection<E>>
```

## Using Validators with Parameters

Validators are typically used when defining parameters:

```kotlin
// Path parameters
val userId by path(ofNonNegativeInt)
val username by path(ofAlphanumeric)

// Query parameters
val limit by query(ofIntRange(1, 100))
val sortBy by query(ofEnum<SortField>())
val email by query(ofEmail)

// Header parameters
val apiKey by header(ofUuid)
val contentType by header(ofNonEmptyString)
```

When used in routes, parameters are automatically validated:

```kotlin
GET("users" / userId) withQuery limit isHandledBy {
    // Access validated parameters
    val id: Int = request[userId]      // Already validated and parsed
    val maxItems: Int = request[limit] // Already validated and parsed
    
    usersRepository.getUsers(id, maxItems).ok
}
```

## Creating Custom Validators

While built-in validators cover many common cases, you'll often need custom validators for domain-specific types. Snitch makes this straightforward:

### Basic Custom Validator

```kotlin
// Define a domain type
data class UserId(val value: String)

// Create a validator
val ofUserId = validator<String, UserId>(
    "valid user ID",
    """^[a-zA-Z0-9]{8,12}$""".toRegex()
) {
    UserId(it)
}

// Use it with a parameter
val userId by path(ofUserId)
```

### Full Custom Validator Implementation

For more complex validation logic:

```kotlin
object UserIdValidator : Validator<String, UserId> {
    override val description = "Valid user ID (8-12 alphanumeric characters)"
    override val regex = """^[a-zA-Z0-9]{8,12}$""".toRegex()
    override val parse: Parser.(Collection<String>) -> UserId = { collection ->
        val value = collection.first()
        if (userRepository.exists(value)) {
            UserId(value)
        } else {
            throw IllegalArgumentException("User ID does not exist")
        }
    }
}

// Use it with a parameter
val userId by path(UserIdValidator)
```

### Factory Functions

Snitch provides several factory functions to create validators:

```kotlin
// For generic validators
fun <From, To> validator(
    description: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    mapper: Parser.(String) -> To
): Validator<From, To>

// For string validators
fun <To> stringValidator(
    description: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    mapper: Parser.(String) -> To
): Validator<String, To>

// For multi-value validators
fun <From, To> validatorMulti(
    description: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    mapper: Parser.(Collection<String>) -> To
): Validator<From, To>

// For string collection validators
fun <To> stringValidatorMulti(
    description: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    mapper: Parser.(Collection<String>) -> To
): Validator<String, To>
```

## Advanced Validator Patterns

### Combining Validation and Business Logic

Sometimes validation involves checking against business rules:

```kotlin
val ofActiveUser = validator<String, User>(
    "active user ID",
    """^[a-zA-Z0-9]{8,12}$""".toRegex()
) {
    val user = userRepository.findById(it) 
        ?: throw IllegalArgumentException("User not found")
        
    if (!user.isActive) {
        throw IllegalArgumentException("User is not active")
    }
    
    user
}
```

### Chaining Validations

You can chain validations by creating validators that build on others:

```kotlin
val ofEmail = validator<String, String>(
    "email address",
    """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()
) { it }

val ofCorporateEmail = validator<String, String>(
    "corporate email address",
    """^[a-zA-Z0-9._%+-]+@company\.com$""".toRegex()
) { 
    // First validate it's an email
    ofEmail.regex.matchEntire(it) ?: throw IllegalArgumentException("Invalid email format")
    
    // Then check for specific domain
    if (!it.endsWith("@company.com")) {
        throw IllegalArgumentException("Must be a company.com email")
    }
    
    it
}
```

### JWT Validators

Here's an example of a JWT validator:

```kotlin
data class JwtClaims(val userId: String, val roles: List<String>)

sealed interface Authentication {
    data class Authenticated(val claims: JwtClaims) : Authentication
    sealed interface Unauthenticated : Authentication
    object InvalidToken : Unauthenticated
    object ExpiredToken : Unauthenticated
    object MissingToken : Unauthenticated
}

val validAccessToken = stringValidator<Authentication>("valid JWT") { jwt ->
    try {
        val jwtVerifier = JWT.require(Algorithm.HMAC256(secretKey))
            .withIssuer("auth-service")
            .build()
            
        val decodedJWT = jwtVerifier.verify(jwt)
        val userId = decodedJWT.getClaim("userId").asString()
        val roles = decodedJWT.getClaim("roles").asList(String::class.java)
        
        Authentication.Authenticated(JwtClaims(userId, roles))
    } catch (e: TokenExpiredException) {
        Authentication.ExpiredToken
    } catch (e: Exception) {
        Authentication.InvalidToken
    }
}

// Use it with a parameter
val accessToken by header(validAccessToken, name = "Authorization")
```

## Handling Collections and Optional Values

### Multiple Values

For parameters that accept multiple values:

```kotlin
val tags by query(ofStringSet)
val roles by query(ofRepeatableEnum<UserRole>())

// In the handler
val userTags: Set<String> = request[tags]
val userRoles: Collection<UserRole> = request[roles]
```

### Optional Parameters

For optional parameters:

```kotlin
// Nullable parameter
val search by optionalQuery(ofNonEmptyString)

// Parameter with default value
val limit by optionalQuery(ofIntRange(1, 100), default = 20)

// Control empty and invalid handling
val page by optionalQuery(
    ofNonNegativeInt, 
    default = 1, 
    emptyAsMissing = true,  // Treat empty string as missing
    invalidAsMissing = true // Use default if parsing fails
)

// In the handler
val searchTerm: String? = request[search] // Nullable
val maxItems: Int = request[limit]       // Always has value (default if missing)
val pageNumber: Int = request[page]      // Has default if empty or invalid
```

## Best Practices

### 1. Use Domain Types

Instead of primitives, use domain-specific types with validators:

```kotlin
// Bad
val userId by path(ofNonEmptyString)

// Good
data class UserId(val value: String)
val ofUserId = validator<String, UserId>("user ID") { UserId(it) }
val userId by path(ofUserId)
```

### 2. Provide Clear Error Messages

When validation fails, provide clear, actionable error messages:

```kotlin
val ofWeekday = validator<String, DayOfWeek>(
    "weekday name (Monday-Friday)",
    """^[A-Za-z]+$""".toRegex()
) {
    try {
        DayOfWeek.valueOf(it.uppercase())
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("'$it' is not a valid weekday (Monday-Friday)")
    }
}
```

### 3. Keep Validators Reusable

If a validation logic is used in multiple places, define it once and reuse:

```kotlin
// Shared across multiple endpoints/controllers
object Validators {
    val ofEmail = validator<String, String>(
        "email address",
        """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()
    ) { it }
    
    val ofZipCode = validator<String, String>(
        "ZIP code",
        """^\d{5}(-\d{4})?$""".toRegex()
    ) { it }
}
```

### 4. Validate at the Edge

Catch invalid inputs at the HTTP layer rather than deep in business logic:

```kotlin
// Let Snitch handle validation
val email by query(ofEmail)

// In the handler - already validated and safe to use
val emailAddress = request[email]
```

### 5. Test Your Validators

Create unit tests for your validators, especially custom ones:

```kotlin
@Test
fun `ofEmail validator should accept valid email addresses`() {
    val validEmails = listOf(
        "user@example.com",
        "firstname.lastname@example.com",
        "user+tag@example.com"
    )
    
    validEmails.forEach { email ->
        assertTrue(ofEmail.regex.matches(email))
    }
}

@Test
fun `ofEmail validator should reject invalid email addresses`() {
    val invalidEmails = listOf(
        "",
        "user@",
        "@example.com",
        "user@example"
    )
    
    invalidEmails.forEach { email ->
        assertFalse(ofEmail.regex.matches(email))
    }
}
```

## Conclusion

Validators are a powerful feature of Snitch that ensure your HTTP inputs are properly validated and transformed. By using validators effectively, you can:

- Create more robust APIs with clear, consistent validation
- Transform raw HTTP inputs into domain-specific types
- Generate accurate API documentation automatically
- Reduce boilerplate validation code in your handlers
- Enforce validation at the edge of your application

Remember that validators are not just for validation but also for transformation. Using them effectively enables you to work with strongly typed values throughout your codebase, making your application more maintainable and less error-prone.