---
slug: typesafe-validation-with-snitch
title: "Type-Safe Validation: Turning Runtime Errors into Compile-Time Safety"
authors: [snitch-team]
tags: [snitch, validation, type-safety, kotlin]
---

# Type-Safe Validation: Turning Runtime Errors into Compile-Time Safety

One of the most common sources of bugs in web applications is improper handling of user input. Traditional frameworks often leave validation as an afterthought, resulting in runtime errors that could have been caught earlier. Snitch takes a different approach, making validation a first-class concern with compile-time safety.

<!-- truncate -->

## The Problem with Traditional Validation

Most web frameworks handle validation in ways that delay error detection:

1. **Runtime validation** that only fails when code executes
2. **String-based configurations** that aren't checked by the compiler
3. **Separate validation layers** disconnected from handler code
4. **Type erasure** that loses information about what's being validated

These approaches lead to a familiar pattern: write code, run application, discover validation errors, fix them, repeat. This cycle is not only inefficient but can let bugs slip through to production.

## Snitch's Type-Safe Validation Approach

Snitch addresses these issues through its validator system:

```kotlin
// Define a domain type
data class UserId(val value: String)

// Create a validator that both validates and transforms
val ofUserId = validator<String, UserId>(
    "valid user ID",
    """^[a-zA-Z0-9]{8,12}$""".toRegex()
) {
    UserId(it)
}

// Use it with a parameter
val userId by path(ofUserId)

// Access the validated parameter
val getUser by handling {
    val id: UserId = request[userId] // Already validated and transformed
    userRepository.findById(id)
}
```

This approach offers several immediate benefits:

1. **Combined validation and transformation** - The validator both checks input and converts it to your domain type
2. **Type safety throughout** - Your handler code works with properly typed values, not raw strings
3. **Early validation failures** - Invalid inputs are rejected before reaching your business logic
4. **Self-documenting code** - The validation requirements are clear from the validator definition

## Extending to Complex Validation

Snitch's validator system scales elegantly to more complex scenarios:

```kotlin
// Advanced email validator with domain restrictions
val ofCorporateEmail = validator<String, Email>(
    "corporate email address",
    """^[a-zA-Z0-9._%+-]+@company\.com$""".toRegex()
) { 
    if (!it.endsWith("@company.com")) {
        throw ValidationException("Must be a company.com email")
    }
    
    Email(it)
}

// Combined validators for a request body
data class SignupRequest(val name: String, val email: String, val age: Int)

val ofSignupRequest = bodyValidator<SignupRequest>("valid signup") { body ->
    // Validate all fields
    if (body.name.isEmpty()) throw ValidationException("Name cannot be empty")
    if (!isValidEmail(body.email)) throw ValidationException("Invalid email format")
    if (body.age < 18) throw ValidationException("Must be 18 or older")
    
    // Return validated object
    body
}
```

## Automatic Error Responses

When validation fails, Snitch automatically generates appropriate error responses:

```
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": "Validation failed",
  "details": {
    "email": "Invalid email format",
    "age": "Must be 18 or older"
  }
}
```

No need to write custom error handling code for each validator - the framework handles this for you, producing consistent, informative error messages for API consumers.

## From Strings to Domain Types

Perhaps the most powerful aspect of Snitch's validator system is how it bridges the gap between raw HTTP inputs (which are always strings) and your domain model:

```kotlin
// Transform path parameter strings into domain types
val orderId by path(ofOrderId)
val status by query(ofOrderStatus)

GET("orders" / orderId) withQuery status isHandledBy {
    // Work with domain types directly
    val id: OrderId = request[orderId]
    val orderStatus: OrderStatus = request[status]
    
    orderRepository.findOrder(id, orderStatus)
}
```

This type-safe approach eliminates an entire category of bugs and makes your code more readable and maintainable.

## Conclusion

By making validation type-safe and integrated into the parameter definition process, Snitch transforms what is traditionally a source of runtime errors into compile-time safety. This shift not only catches issues earlier in the development process but also leads to more robust, self-documenting code that's easier to maintain and evolve over time.

The next time you find yourself chasing down an "Invalid input" error in production, remember that with the right framework, that bug could have been caught before your code even compiled.