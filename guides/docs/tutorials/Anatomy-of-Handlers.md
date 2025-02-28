# Anatomy of the Snitch Handler DSL

Handlers are the core of your API's business logic in Snitch. They process incoming requests, execute application code, and produce responses. This guide explores the inner workings of Snitch's Handler DSL, explaining how handlers are defined, composed, and integrated with the rest of the framework.

## Table of Contents

- [Handler Fundamentals](#handler-fundamentals)
- [The RequestWrapper](#the-requestwrapper)
- [Response Creation](#response-creation)
  - [Response Extensions](#response-extensions)
  - [Status Codes](#status-codes)
- [Handler Definition Approaches](#handler-definition-approaches)
  - [Inline Handlers](#inline-handlers)
  - [Named Handlers](#named-handlers)
  - [Reusable Handlers](#reusable-handlers)
- [Type-Safe Parameter Access](#type-safe-parameter-access)
- [Body Handling](#body-handling)
- [Error Handling in Handlers](#error-handling-in-handlers)
- [Asynchronous Handlers](#asynchronous-handlers)
- [The Handler Execution Pipeline](#the-handler-execution-pipeline)
- [Testing Handlers](#testing-handlers)
- [Best Practices](#best-practices)

## Handler Fundamentals

At its core, a handler in Snitch is simply a function that:
1. Takes a `RequestWrapper` as its receiver (via Kotlin's function literal with receiver syntax)
2. Returns a value that can be converted to an HTTP response

The basic signature looks like this:

```kotlin
typealias Handler<T> = RequestWrapper.() -> T
```

This simple design allows handlers to:
- Access request data through the `RequestWrapper` receiver
- Return any type that can be serialized to a response
- Leverage Kotlin's powerful type system

Let's explore how this works in practice.

## The RequestWrapper

The `RequestWrapper` is the handler's interface to the incoming request. It provides access to:

```kotlin
interface RequestWrapper {
    val method: Method
    val path: String
    val queryParams: Map<String, Collection<String>>
    val headerParams: Map<String, Collection<String>>
    val pathParams: Map<String, String>
    val parser: Parser
    val attributes: MutableMap<String, Any>
    
    operator fun <T> get(parameter: Parameter<*, T>): T
    
    // Additional utility methods...
}
```

The key components are:

1. **Request metadata**: Method, path, and raw parameter maps
2. **Parser**: For converting strings to/from structured data
3. **Attributes**: A mutable map for storing request-scoped data
4. **Parameter access**: Type-safe access to validated parameters via the `get` operator

The `get` operator is particularly important, as it provides type-safe access to parameters:

```kotlin
val userId = request[userId] // Returns the validated parameter value with correct type
```

Under the hood, this operator:
1. Looks up the parameter's value(s) from the appropriate source (path, query, header, body)
2. Validates the value(s) using the parameter's validator
3. Transforms the value(s) to the target type
4. Returns the strongly-typed result

If validation fails, the framework automatically returns an appropriate error response.

## Response Creation

Snitch provides a rich DSL for creating HTTP responses. The core mechanism is extension properties and functions on any type, which create subclasses of the sealed `HttpResponse` class.

### The HttpResponse Hierarchy

`HttpResponse` is a sealed class with three implementations:

```kotlin
sealed class HttpResponse<T, out S : StatusCodes> {
    abstract val statusCode: StatusCodes
    abstract val headers: Map<String, String>
    abstract val value: context(Parser) () -> Any?
    abstract fun header(header: Pair<String, String>): HttpResponse<T, S>
    
    // Provides a way to transform responses based on their type
    fun map(
        failure: ErrorHttpResponse<T, *, S>.() -> HttpResponse<Any, *> = { this as HttpResponse<Any, *> },
        success: SuccessfulHttpResponse<T, S>.() -> HttpResponse<Any, *>,
    ): HttpResponse<Any, *> = when (this) {
        is SuccessfulHttpResponse -> this.success()
        is ErrorHttpResponse<T, *, S> -> this.failure()
        is RawHttpResponse -> throw UnsupportedOperationException()
    }
}
```

The three implementations are:

1. **SuccessfulHttpResponse**: For 2xx status codes, with typed body content
   ```kotlin
   data class SuccessfulHttpResponse<T, out S : StatusCodes>(
       override val statusCode: S,
       val body: T,
       val _format: Format = Format.Json,
       override val value: context(Parser) () -> Any? = { /* serialization logic */ },
       override val headers: Map<String, String> = emptyMap(),
   ) : HttpResponse<T, S>()
   ```

2. **ErrorHttpResponse**: For error status codes (4xx, 5xx), with typed error details
   ```kotlin
   data class ErrorHttpResponse<T, E, out S : StatusCodes>(
       override val statusCode: StatusCodes,
       val details: E,
       override val value: context(Parser) () -> Any? = { details?.serialized },
       override val headers: Map<String, String> = emptyMap(),
   ) : HttpResponse<T, S>()
   ```

3. **RawHttpResponse**: For sending raw content with minimal processing
   ```kotlin
   data class RawHttpResponse<T, out S : StatusCodes>(
       override val statusCode: S,
       val body: Any,
       val _format: Format = Format.Json,
       override val value: context(Parser) () -> Any? = { body },
       override val headers: Map<String, String> = emptyMap(),
   ) : HttpResponse<T, S>()
   ```

### Response Extensions

The `CommonResponses` interface provides extension properties and functions to create responses with minimal syntax:

```kotlin
// Success responses
val <T> T.ok get() = SuccessfulHttpResponse(StatusCodes.OK, this)
val <T> T.created get() = SuccessfulHttpResponse(StatusCodes.CREATED, this)
val <T> T.accepted get() = SuccessfulHttpResponse(StatusCodes.ACCEPTED, this)
val <T> T.noContent get() = SuccessfulHttpResponse(StatusCodes.NO_CONTENT, this)

// Error responses
fun <T, E, S: StatusCodes> E.badRequest() = ErrorHttpResponse<T, _, S>(StatusCodes.BAD_REQUEST, this)
fun <T, E, S: StatusCodes> E.unauthorized() = ErrorHttpResponse<T, _,S>(StatusCodes.UNAUTHORIZED, this)
fun <T, E, S: StatusCodes> E.forbidden() = ErrorHttpResponse<T, _, S>(StatusCodes.FORBIDDEN, this)
fun <T, E, S: StatusCodes> E.notFound() = ErrorHttpResponse<T, _, S>(StatusCodes.NOT_FOUND, this)
fun <T, E, S: StatusCodes> E.serverError() = ErrorHttpResponse<T, _, S>(StatusCodes.INTERNAL_SERVER_ERROR, this)

// Format control
fun <T, S : StatusCodes> HttpResponse<T, S>.format(newFormat: Format) = /* ... */
val <T, S : StatusCodes> HttpResponse<T, S>.plainText get() = /* ... */
```

This allows handlers to return responses in a concise, readable way:

```kotlin
val getUser by handling {
    val user = userRepository.findById(request[userId])
    if (user != null) user.ok
    else "User not found".notFound()
}
```

### Status Codes

Status codes are modeled as singleton objects within the `StatusCodes` abstract class:

```kotlin
abstract class StatusCodes(val code: Int = 200) {
    object OK : StatusCodes(200)
    object CREATED : StatusCodes(201)
    object ACCEPTED : StatusCodes(202)
    object NO_CONTENT : StatusCodes(204)
    
    object BAD_REQUEST : StatusCodes(400)
    object UNAUTHORIZED : StatusCodes(401)
    object FORBIDDEN : StatusCodes(403)
    object NOT_FOUND : StatusCodes(404)
    object TOO_MANY_REQUESTS : StatusCodes(429)
    
    object INTERNAL_SERVER_ERROR : StatusCodes(500)
    object SERVICE_UNAVAILABLE : StatusCodes(503)
    // Plus many other standard HTTP status codes
}
```

When the response is rendered, the framework:
1. Sets the HTTP status code to `statusCode.code`
2. Serializes the body or details based on the response type and format
3. Adds any custom headers defined in the response

This design provides several advantages:
- Type safety through generics
- Clear distinction between success and error responses
- Flexible content format handling (JSON, plain text, etc.)
- Support for response transformation with the `map` function

## Handler Definition Approaches

Snitch offers several ways to define handlers, each with its own use cases.

### Inline Handlers

The simplest approach is defining handlers inline with routes:

```kotlin
GET("users" / userId) isHandledBy {
    userRepository.findById(request[userId])
        ?.ok
        ?: "User not found".notFound()
}
```

This works well for simple endpoints with minimal logic.

### Named Handlers

For more complex endpoints, you can define named handlers:

```kotlin
val getUser by handling {
    val id = request[userId]
    val user = userRepository.findById(id)
    if (user != null) user.ok
    else "User not found".notFound()
}

// In routes
GET("users" / userId) isHandledBy getUser
```

The `handling` function is defined as:

```kotlin
fun <T: Any> handling(handler: Handler<T>): HandlerReference<T> =
    HandlerReference(handler)

class HandlerReference<T: Any>(val handler: Handler<T>)
```

This allows handlers to be:
- Reused across multiple routes
- Tested independently
- Named for better code organization

### Reusable Handlers

For handlers that need access to request body data, there's a specialized approach:

```kotlin
val createUser by parsing<CreateUserRequest>() handling {
    // body is the parsed CreateUserRequest
    val result = userService.createUser(body.name, body.email)
    CreatedUser(result.id, result.name).created
}
```

The `parsing` function is:

```kotlin
inline fun <reified B: Any> parsing(): BodyHandlerBuilder<B> =
    BodyHandlerBuilder(B::class)

class BodyHandlerBuilder<B: Any>(private val bodyClass: KClass<B>) {
    infix fun handling(handler: BodyHandler<B, *>): BodyHandlerReference<B, *> =
        BodyHandlerReference(handler, bodyClass)
}

typealias BodyHandler<B, T> = BodyRequestWrapper<B>.() -> T
```

This pattern provides:
- Type-safe access to the request body
- Automatic body parsing and validation
- Clear separation of body handling from other request processing

## Type-Safe Parameter Access

One of Snitch's core strengths is type-safe parameter access. When you define a parameter:

```kotlin
val userId by path(ofLong)
```

You can access it with the correct type in handlers:

```kotlin
val getUser by handling {
    val id: Long = request[userId] // Type is Long, not String
    // ...
}
```

Behind the scenes, this works through the interaction of:
1. The `Parameter` class that captures the validator
2. The `get` operator on `RequestWrapper` that applies the validator
3. Kotlin's type inference that understands the return type

Let's explore how parameters are implemented:

```kotlin
class Parameter<From, To>(
    val name: String,
    val description: String,
    val validator: Validator<From, To>,
    val source: ParameterSource,
    val required: Boolean = true,
    val default: To? = null,
    // Additional metadata...
)
```

When you access a parameter, the framework:
1. Extracts the raw value from the request based on `source`
2. Validates and transforms it using `validator`
3. Falls back to `default` if the parameter is missing and not `required`

This ensures that by the time your handler code executes, all parameters are valid and properly typed.

## Body Handling

Request bodies require special treatment due to their potentially complex structure:

```kotlin
data class CreateUserRequest(val name: String, val email: String)

val userBody by body<CreateUserRequest>()

POST("users") with userBody isHandledBy {
    val request: CreateUserRequest = request[userBody]
    // Use typed request body
}
```

The `body` function creates a special parameter that:
1. Extracts the raw request body from the HTTP request
2. Uses the configured JSON parser to deserialize it to the target type
3. Makes it available via `request[bodyParam]`

For handlers that need frequent body access, the `parsing` pattern simplifies this:

```kotlin
val createUser by parsing<CreateUserRequest>() handling {
    // Direct access to body without request[bodyParam]
    val user = userService.createUser(body.name, body.email)
    // ...
}
```

Under the hood, `parsing` creates a specialized `BodyRequestWrapper` that exposes the parsed body:

```kotlin
interface BodyRequestWrapper<B> : RequestWrapper {
    val body: B
}
```

This provides a cleaner API for body-centric handlers.

## Error Handling in Handlers

Handlers can approach error handling in several ways:

### 1. Return explicit error responses:

```kotlin
val getUser by handling {
    try {
        val user = userService.findById(request[userId])
        if (user != null) user.ok
        else "User not found".notFound()
    } catch (e: DatabaseException) {
        "Database error: ${e.message}".serverError()
    }
}
```

### 2. Throw exceptions that are handled globally:

```kotlin
// In application setup
snitch(parser)
    .onRoutes(routes)
    .handleException(ResourceNotFoundException::class) { ex ->
        ErrorResponse(ex.message ?: "Resource not found").notFound()
    }
    .handleException(DatabaseException::class) { ex ->
        ErrorResponse("Internal error").serverError()
    }
    .start()

// In handler - let exceptions propagate
val getUser by handling {
    userService.findById(request[userId])?.ok
        ?: throw ResourceNotFoundException("User not found")
}
```

The exception handling is implemented by wrapping handler execution:

```kotlin
try {
    handler.invoke(requestWrapper)
} catch (e: Exception) {
    // Find appropriate exception handler
    exceptionHandlers[e::class]?.invoke(e) ?: throw e
}
```

This allows for centralized error handling while keeping handlers focused on the happy path.

## Asynchronous Handlers

Snitch supports asynchronous handlers through coroutines:

```kotlin
val getUser by coHandling {
    // Suspend function call
    val user = userRepository.findByIdAsync(request[userId])
    user?.ok ?: "User not found".notFound()
}
```

The `coHandling` function is defined as:

```kotlin
fun <T: Any> coHandling(handler: suspend Handler<T>): CoHandlerReference<T> =
    CoHandlerReference(handler)
```

This leverages Kotlin's coroutine support to allow non-blocking execution while maintaining the same expressive handler syntax.

## The Handler Execution Pipeline

When a request is processed, handlers go through a specific execution pipeline:

1. **Parameter Validation**: All required parameters are validated
2. **Condition Evaluation**: All conditions are checked
3. **Before Actions**: Before actions execute in reverse order
4. **Decoration Setup**: Decorations wrap the handler
5. **Handler Execution**: The handler processes the request
6. **After Actions**: After actions execute in declaration order

This pipeline ensures that by the time your handler executes:
- All parameters are validated and available
- All conditions are satisfied
- Any pre-processing logic has run

The handler's result then flows through:
1. Any transformation logic in decorations
2. Serialization based on content negotiation
3. Response status code and header application

This structured flow keeps handlers focused on business logic while the framework handles HTTP concerns.

## Testing Handlers

Snitch's design makes handler testing straightforward:

```kotlin
@Test
fun `getUser returns user when found`() {
    // Setup mock repository
    val mockRepo = mockk<UserRepository>()
    every { mockRepo.findById(1) } returns User(1, "Test User")
    
    // Create test request wrapper
    val request = TestRequestWrapper().apply {
        // Set up parameter for testing
        setParameter(userId, 1L)
    }
    
    // Execute handler directly
    val handler = UserHandlers(mockRepo).getUser.handler
    val response = handler.invoke(request)
    
    // Verify response
    assertThat(response.statusCode).isEqualTo(StatusCode.OK)
    assertThat(response.value).isInstanceOf(User::class.java)
    assertThat((response.value as User).name).isEqualTo("Test User")
}
```

For more comprehensive testing, Snitch also provides a testing DSL:

```kotlin
@Test
fun `getUser endpoint returns user when found`() {
    // Setup mock repository
    val mockRepo = mockk<UserRepository>()
    every { mockRepo.findById(1) } returns User(1, "Test User")
    
    // Test the endpoint
    testApp {
        // Register routes with mock dependencies
        registerRoutes(userRoutes(mockRepo))
        
        // Execute request
        GET("/users/1")
            .expectCode(200)
            .expectJson {
                it.path("id").asInt() shouldBe 1
                it.path("name").asText() shouldBe "Test User"
            }
    }
}
```

This approach allows for both unit testing of individual handlers and integration testing of entire endpoints.

## Handler Implementation Details

Let's look at some of the key implementation details behind handlers:

### Handler Function Types

Snitch uses several function types for different handler scenarios:

```kotlin
// Basic handler
typealias Handler<T> = RequestWrapper.() -> T

// Body handler
typealias BodyHandler<B, T> = BodyRequestWrapper<B>.() -> T

// Coroutine handler
typealias CoHandler<T> = suspend Handler<T>
```

### Handler References

Handler references wrap handler functions to make them usable with the DSL:

```kotlin
class HandlerReference<T: Any>(val handler: Handler<T>)

class BodyHandlerReference<B: Any, T: Any>(
    val handler: BodyHandler<B, T>,
    val bodyClass: KClass<B>
)

class CoHandlerReference<T: Any>(val handler: suspend Handler<T>)
```

### Handler Execution

The endpoint processor executes handlers through a series of steps:

```kotlin
// Simplified flow
fun executeHandler(endpoint: Endpoint<*>, request: Request): Response {
    // Create request wrapper
    val wrapper = RequestWrapperImpl(request, parser)
    
    // Validate parameters
    validateParameters(endpoint.parameters, wrapper)
    
    // Check conditions
    evaluateConditions(endpoint.conditions, wrapper)
    
    // Execute before actions
    for (action in endpoint.beforeActions.reversed()) {
        val earlyResponse = action(wrapper)
        if (earlyResponse != null) return earlyResponse
    }
    
    // Execute handler with decorations
    val result = applyDecorations(endpoint.decorations, endpoint.handler!!)(wrapper)
    
    // Convert result to response
    val response = when (result) {
        is Response -> result
        else -> Response(result, OK)
    }
    
    // Execute after actions
    for (action in endpoint.afterActions) {
        action(wrapper)
    }
    
    return response
}
```

This structured approach ensures consistent request handling while giving handlers access to all the information they need.

## Best Practices

Based on the inner workings of handlers, here are some best practices:

### 1. Keep Handlers Focused

Handlers should focus on their primary responsibility:

```kotlin
// Good: Focused on user retrieval
val getUser by handling {
    userRepository.findById(request[userId])
        ?.ok
        ?: "User not found".notFound()
}

// Avoid: Mixing concerns
val getUser by handling {
    // Authentication logic
    if (!isAuthenticated()) return "Unauthorized".unauthorized()
    
    // Logging
    logger.info("Getting user ${request[userId]}")
    
    // Business logic
    val user = userRepository.findById(request[userId])
    
    // Response creation
    if (user != null) user.ok
    else "User not found".notFound()
}
```

Use conditions, decorations, and actions for cross-cutting concerns.

### 2. Leverage Type Safety

Take advantage of Snitch's type safety features:

```kotlin
// Define parameters with specific types
val userId by path(ofLong)
val userRole by query(ofEnum<UserRole>())

// Use typed parameters in handlers
val getUser by handling {
    val id: Long = request[userId]
    val role: UserRole = request[userRole]
    // No need for manual parsing or validation
}
```

### 3. Use Named Handlers for Complex Logic

```kotlin
// Named handler for better organization
val createUser by parsing<CreateUserRequest>() handling {
    // Validation
    if (!isValidEmail(body.email)) {
        return ValidationError("Invalid email").badRequest()
    }
    
    // Business logic
    try {
        val id = userService.createUser(body.name, body.email)
        CreatedUser(id).created
    } catch (e: DuplicateUserException) {
        ValidationError("User already exists").badRequest()
    }
}
```

### 4. Structure Error Handling Appropriately

For application-specific exceptions, use global handlers:

```kotlin
snitch(parser)
    .handleException(ResourceNotFoundException::class) { ex ->
        ErrorResponse(ex.message ?: "Resource not found").notFound()
    }

// Then throw from handlers
val getUser by handling {
    userRepository.findById(request[userId]) 
        ?: throw ResourceNotFoundException("User not found")
}
```

For business logic validation, return explicit responses:

```kotlin
val createUser by parsing<CreateUserRequest>() handling {
    if (!isValidEmail(body.email)) {
        return ValidationError("Invalid email").badRequest()
    }
    // Proceed with valid input
}
```

### 5. Use Coroutines for I/O-Bound Operations

```kotlin
val getUser by coHandling {
    // Non-blocking database call
    val user = userRepository.findByIdAsync(request[userId])
    user?.ok ?: "User not found".notFound()
}
```

### 6. Group Related Handlers

```kotlin
class UserHandlers(private val userRepository: UserRepository) {
    val getUser by handling {
        // Implementation
    }
    
    val createUser by parsing<CreateUserRequest>() handling {
        // Implementation
    }
    
    val updateUser by parsing<UpdateUserRequest>() handling {
        // Implementation
    }
    
    val deleteUser by handling {
        // Implementation
    }
}

// In routes
val userHandlers = UserHandlers(userRepository)

"users" / {
    GET(userId) isHandledBy userHandlers.getUser
    POST() with userBody isHandledBy userHandlers.createUser
    PUT(userId) with userBody isHandledBy userHandlers.updateUser
    DELETE(userId) isHandledBy userHandlers.deleteUser
}
```

This approach:
- Groups related functionality
- Makes dependency injection straightforward
- Improves code organization

## Conclusion

Snitch's Handler DSL provides a powerful, type-safe way to implement API business logic. By understanding its internal workings, you can leverage its full potential to create expressive, maintainable handlers.

The combination of type-safe parameter access, flexible response creation, and structured execution pipeline allows you to focus on your business logic while the framework handles HTTP concerns.

Whether you're writing simple endpoints or complex business processes, Snitch's Handler DSL offers the tools to express your intent clearly and safely.