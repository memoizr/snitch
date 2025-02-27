---
slug: automatic-api-docs-with-snitch
title: "Never Write API Docs Again: Snitch's Zero-Effort Documentation"
authors: [snitch-team]
tags: [snitch, documentation, openapi, developer-experience]
---

# Never Write API Docs Again: Snitch's Zero-Effort Documentation

Documentation is crucial for API adoption and maintenance, yet it's often treated as an afterthought. Frameworks typically require extensive annotation or separate configuration files to generate documentation, leading to documentation that quickly becomes outdated. Snitch takes a radical approach: what if your code could generate complete, accurate documentation with zero additional effort?

<!-- truncate -->

## The Documentation Problem

Most developers are familiar with the documentation dilemma:

1. **Manual documentation** is time-consuming and quickly becomes outdated
2. **Annotation-based documentation** creates clutter and requires constant maintenance
3. **Separate documentation files** drift from the actual implementation
4. **Incomplete documentation** leads to trial-and-error API usage

The result is frustration for both API developers and consumers, wasted time, and buggy integrations.

## Snitch's Zero-Effort Documentation Approach

Snitch's approach is radical in its simplicity: your code is your documentation. With a single line, you can generate and serve complete OpenAPI 3.0 documentation:

```kotlin
snitch(GsonJsonParser)
    .onRoutes(routes)
    .generateDocumentation()
    .servePublicDocumentation()
    .start()
```

This automatically creates interactive Swagger UI documentation available at `/docs`, with no additional code or configuration required.

## What Gets Documented Automatically

Snitch's documentation includes everything API consumers need to know:

### 1. Routes and Methods

The complete API structure is documented, with all paths and supported HTTP methods:

```kotlin
routes {
    "api" / "users" / {
        GET() isHandledBy getAllUsers          // GET /api/users
        POST() with userBody isHandledBy createUser  // POST /api/users
        
        userId / {
            GET() isHandledBy getUser          // GET /api/users/{userId}
            PUT() with userBody isHandledBy updateUser  // PUT /api/users/{userId}
        }
    }
}
```

### 2. Parameters with Validation Rules

All parameters are documented with their validation rules:

```kotlin
val userId by path(ofUUID, description = "Unique user identifier")
val limit by query(ofIntRange(1, 100), default = 20, description = "Maximum number of results")
val orderBy by query(ofEnum<SortField>(), description = "Field to sort results by")
```

The generated documentation shows:
- Parameter name, location (path, query, header)
- Data type and format (string, integer, UUID, etc.)
- Validation constraints (min/max, pattern, enum values)
- Default values for optional parameters
- Custom descriptions if provided

### 3. Request Bodies

Request body schemas are automatically generated from your Kotlin classes:

```kotlin
data class CreateUserRequest(
    val name: String,
    val email: String,
    val age: Int
)

val userBody by body<CreateUserRequest>()
```

The documentation includes the complete JSON schema with all properties, types, and optional custom descriptions.

### 4. Response Types and Status Codes

Response types are inferred from your handler code:

```kotlin
GET(userId) isHandledBy {
    val user = userRepository.findById(request[userId])
    if (user != null) user.ok
    else "User not found".notFound()
}
```

This documents:
- 200 OK response with the User schema
- 404 Not Found response with a string message

### 5. Authorization Requirements

Security requirements are documented based on your conditions:

```kotlin
GET("admin/dashboard") onlyIf hasAdminRole isHandledBy { getDashboard() }
```

The documentation will show that this endpoint requires admin role authorization.

## Enhancing the Documentation

While zero-effort documentation is powerful, Snitch also allows for enhancements:

```kotlin
data class User(
    @Description("Unique identifier") val id: UUID,
    @Description("User's full name") val name: String,
    @Description("User's email address") val email: String,
    @Example("true") val active: Boolean
)

val userId by path(
    ofUUID,
    description = "User's unique identifier",
    example = "123e4567-e89b-12d3-a456-426614174000"
)
```

These annotations and parameters don't affect the functionality but enhance the documentation with additional context and examples.

## Documentation as a Living Artifact

The most powerful aspect of Snitch's approach is that documentation becomes a living artifact that automatically stays in sync with your code:

1. **Add a new endpoint?** It appears in the docs immediately.
2. **Change a parameter type?** The documentation updates automatically.
3. **Add a new response status?** It's reflected in the docs with no extra work.

This ensures your documentation is always complete and accurate, eliminating the "documentation drift" that plagues many APIs.

## Real World Impact

The impact of automatic documentation extends beyond convenience:

- **Faster onboarding** for new team members and API consumers
- **Reduced support burden** as API behavior is clearly documented
- **More consistent APIs** as documentation highlights inconsistencies
- **Better developer experience** for API consumers
- **Documentation-driven development** becomes effortless

## Conclusion

Snitch's zero-effort documentation approach challenges the conventional wisdom that comprehensive API documentation requires significant additional work. By deriving documentation directly from your code, Snitch ensures your documentation is always complete, accurate, and up-to-date.

Next time you're manually updating API documentation or adding yet another annotation to describe your endpoints, remember that there's an alternative approach where documentation simply happens automatically.

Give your API consumers the gift of always-accurate, comprehensive documentation - without spending a minute of your time maintaining it.