# Anatomy of the Snitch Validator DSL

Validators are a cornerstone of Snitch's design, ensuring that HTTP inputs are properly validated and transformed into domain types. This guide explores the internal workings of the validator DSL, explaining each component and how they fit together.

## The Validator Interface

At the heart of the validation system is the `Validator` interface:

```kotlin
interface Validator<T, R> {
    val regex: Regex
    val description: String
    val parse: Parser.(Collection<String>) -> R
    fun optional(): Validator<T?, R?> = this as Validator<T?, R?>
}
```

Let's break down each component:

1. **Type Parameters**:
   - `T`: The input type that the validator accepts (typically `String`)
   - `R`: The output type that the validator produces (your domain type)

2. **Properties**:
   - `regex`: A regular expression used for initial string validation
   - `description`: A human-readable description used for documentation
   - `parse`: A function that takes a collection of strings and transforms them into the output type
   
3. **Methods**:
   - `optional()`: Converts a required validator to an optional one

The interface is intentionally minimal, focusing on the essential components of validation: pattern matching, transformation, and documentation.

## Creating Validators

Snitch provides several factory functions for creating validators with different behaviors:

### The `validator` Function

The most general factory function:

```kotlin
inline fun <From, To> validator(
    descriptions: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    crossinline mapper: Parser.(String) -> To
) = object : Validator<From, To> {
    override val description = descriptions
    override val regex = regex
    override val parse: Parser.(Collection<String>) -> To = { mapper(it.single()) }
}
```

This function creates a validator that:
- Has a custom description
- Uses a specified regex (or a default that matches any non-empty string)
- Applies a mapping function to transform the input

The `crossinline` modifier ensures that the mapper function can be used inside a lambda that will be inlined.

**Typical Usage**:

```kotlin
val ofUUID = validator<String, UUID>(
    "valid UUID",
    """^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$""".toRegex(RegexOption.IGNORE_CASE)
) {
    try {
        UUID.fromString(it)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid UUID format")
    }
}
```

### The `stringValidator` Function

A specialized version for string inputs:

```kotlin
inline fun <To> stringValidator(
    description: String = "",
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    crossinline mapper: Parser.(String) -> To,
) = validator<String, To>(description, regex, mapper)
```

This is a convenience function that defaults the input type to `String`, which is the most common case.

**Typical Usage**:

```kotlin
val ofEmail = stringValidator(
    "email address",
    """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$""".toRegex()
) { it }
```

### The `validatorMulti` Function

For handling collections of values:

```kotlin
fun <From, To> validatorMulti(
    descriptions: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    mapper: Parser.(Collection<String>) -> To
) = object : Validator<From, To> {
    override val description = descriptions
    override val regex = regex
    override val parse: Parser.(Collection<String>) -> To = mapper
}
```

This function allows working with multiple input values, such as repeated query parameters.

**Typical Usage**:

```kotlin
val ofStringSet = validatorMulti<String, Set<String>>(
    "set of strings"
) { strings ->
    strings.flatMap { it.split(",") }.toSet()
}
```

### The `stringValidatorMulti` Function

A specialized version for string inputs that return collections:

```kotlin
fun <To> stringValidatorMulti(
    description: String,
    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),
    mapper: Parser.(Collection<String>) -> To,
) = validatorMulti<String, To>(description, regex, mapper)
```

This combines the convenience of `stringValidator` with the collection handling of `validatorMulti`.

**Typical Usage**:

```kotlin
val ofTags = stringValidatorMulti<List<String>>(
    "comma-separated tags"
) { params ->
    params.flatMap { it.split(",") }
        .map { it.trim() }
        .filter { it.isNotEmpty() }
}
```

## How Validators Work

Now that we understand the interface and creation functions, let's explore how validators operate at runtime.

### Regex Validation

The first step in validation is pattern matching using the `regex` property:

```kotlin
// Inside parameter handler code
if (!validator.regex.matches(value)) {
    throw ValidationException("Value doesn't match pattern for ${validator.description}")
}
```

This provides a fast first-pass validation before more complex logic is applied. For example, checking that an email string has a basic email-like structure before attempting further validation.

### Transformation Logic

After regex validation passes, the `parse` function is called with the collection of parameter values:

```kotlin
// Inside parameter handler code
try {
    return validator.parse(parser, values)
} catch (e: Exception) {
    throw ValidationException("Failed to parse ${validator.description}: ${e.message}")
}
```

The parse function is responsible for:
1. Handling single vs. multiple values
2. Converting strings to the target type
3. Performing business-specific validation
4. Throwing exceptions for invalid inputs

The transformation typically has access to the `Parser` instance, which provides useful utilities for working with common formats like JSON.

### Error Handling

Validators report errors by throwing exceptions, which Snitch catches and converts to appropriate HTTP responses (typically 400 Bad Request).

This happens at several levels:
1. **Regex mismatch**: Throws a `ValidationException`
2. **Empty collection**: Throws a `NoSuchElementException` from the `single()` call
3. **Custom validation**: Validator-specific exceptions from the mapper function

Snitch provides automatic handling for all of these, generating clear error messages for API consumers.

## The Parser's Role

You may have noticed that the validator functions all pass a `Parser` instance to the mapper function. The `Parser` is an interface for converting between strings and structured data:

```kotlin
interface Parser {
    fun <T> fromJson(json: String): T
    fun <T> toJson(value: T): String
    fun <T : Enum<T>> String.parse(enumClass: Class<T>): T
}
```

This allows validators to leverage the application's JSON parser for complex transformations, particularly for request bodies.

**Example using the Parser**:

```kotlin
val ofUser = stringValidator<User>("user") {
    parser.fromJson<User>(it)
}
```

This is particularly powerful for body validators, allowing seamless conversion between JSON strings and domain objects.

## Custom Validators

While the factory functions cover most use cases, you can also implement the `Validator` interface directly for complete control:

```kotlin
object UserIdValidator : Validator<String, UserId> {
    override val description = "Valid user ID"
    override val regex = """^[a-zA-Z0-9]{8,12}$""".toRegex()
    override val parse: Parser.(Collection<String>) -> UserId = { collection ->
        val value = collection.first()
        // Custom validation logic
        if (!userRepository.exists(value)) {
            throw IllegalArgumentException("User ID does not exist")
        }
        UserId(value)
    }
}
```

This approach is useful when:
- You need complex validation logic
- You want to encapsulate validation in a self-contained object
- You need to inject dependencies (like repositories) into the validator

## Validator Internals

Let's explore what happens when a validator is used with a parameter:

```kotlin
val userId by path(ofUUID)
```

Here's the sequence of events:

1. The `path` function creates a `Parameter` object, storing the validator
2. When a request arrives, Snitch extracts the raw path parameter value
3. The validator's regex is checked against the value
4. If the regex matches, the parse function is called
5. The parse function converts the string to a UUID
6. The result is cached and made available via `request[userId]`

If any step fails, the request processing is halted, and an error response is returned to the client.

## Best Practices

Based on the internal workings of validators, here are some best practices:

1. **Use specific regex patterns**: The more specific your regex, the faster you can reject invalid inputs
   
2. **Keep transformation functions pure**: Avoid side effects in mapper functions for easier testing and reasoning

3. **Provide clear error messages**: When throwing exceptions, include specific details about why validation failed

4. **Define domain-specific validators**: Create validators for your domain types to encapsulate validation logic

5. **Compose validators**: Build complex validators by combining simpler ones

6. **Avoid heavy computation in validators**: Validators run on every request, so keep them efficient

7. **Use the optional() method**: For truly optional parameters, apply `optional()` to your validator instead of handling nullability in mapper functions

## Putting It All Together

Let's see a complete example of a custom validator used in an endpoint:

```kotlin
// Domain type
data class UserId(val value: String)

// Custom validator
val ofUserId = validator<String, UserId>(
    "valid user ID",
    """^[a-zA-Z0-9]{8,12}$""".toRegex()
) {
    if (it.length < 8 || it.length > 12) {
        throw IllegalArgumentException("User ID must be 8-12 characters long")
    }
    
    if (!it.matches("""^[a-zA-Z0-9]*$""".toRegex())) {
        throw IllegalArgumentException("User ID must contain only letters and numbers")
    }
    
    UserId(it)
}

// Parameter definition
val userId by path(ofUserId)

// Route with validated parameter
GET("users" / userId) isHandledBy {
    // UserId is already validated and transformed
    val id: UserId = request[userId]
    userRepository.findById(id).ok
}
```

This approach ensures:
1. Early validation at the HTTP layer
2. Type-safe access to domain types
3. Clean separation of validation and business logic
4. Clear error messages for API consumers

## Conclusion

The validator DSL in Snitch provides a powerful, type-safe way to transform raw HTTP inputs into domain types. By understanding its internal workings, you can create more robust, maintainable APIs with clear error handling and strong type safety.

Remember that validators aren't just about rejecting invalid inputs—they're about bridging the gap between the untyped world of HTTP and the strongly-typed world of your domain model.