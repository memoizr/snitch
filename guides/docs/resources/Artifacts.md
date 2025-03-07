# Snitch Artifacts

Snitch is modular by design, providing several artifacts that can be used independently based on your needs. All artifacts are published to Maven Central.

## Core Artifacts

### snitch-bootstrap

```kotlin
implementation("io.github.memoizr:snitch-bootstrap:4.0.1")
```

This is the main artifact most users should depend on. It includes everything needed to create a complete web service using Snitch with Undertow as the server and Gson for JSON parsing.

**Dependencies**: Includes core, gsonparser, and undertow modules.

### snitch-core

```kotlin
implementation("io.github.memoizr:snitch-core:4.0.1")
```

The core module contains the essential building blocks of Snitch:
- DSL for defining routes and handlers
- Parameter definition and validation framework
- Middleware and conditions system
- Documentation generation engine

This module is server-agnostic and doesn't include any specific JSON parsing implementation.

### snitch-types

```kotlin
implementation("io.github.memoizr:snitch-types:4.0.1")
```

A lightweight module containing the basic types and interfaces used across the Snitch ecosystem. This module has minimal dependencies and can be used in your domain model to avoid pulling in the entire Snitch framework.

## Extensions and Implementations

### snitch-undertow

```kotlin
implementation("io.github.memoizr:snitch-undertow:4.0.1")
```

Provides Undertow server integration for Snitch. Undertow is a flexible, high-performance web server by JBoss that serves as the default server implementation for Snitch.

### snitch-gsonparser

```kotlin
implementation("io.github.memoizr:snitch-gsonparser:4.0.1")
```

Implements JSON parsing and serialization using Google's Gson library. This module allows Snitch to convert between JSON and Kotlin objects.

### snitch-coroutines

```kotlin
implementation("io.github.memoizr:snitch-coroutines:4.0.1")
```

Adds Kotlin Coroutines support to Snitch, allowing you to define suspending handlers and use the full power of Kotlin's asynchronous programming features.

Requires the `-Xcontext-receivers` compiler flag:

```kotlin
tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xcontext-receivers"
    }
}
```

### snitch-auth

```kotlin
implementation("io.github.memoizr:snitch-auth:4.0.1")
```

Provides authentication and authorization capabilities for Snitch applications, including support for JWT tokens, role-based access control, and security middleware.

### snitch-validation

```kotlin
implementation("io.github.memoizr:snitch-validation:4.0.1")
```

Provides integration with Hibernate Validator (Jakarta Bean Validation), allowing you to use standard validation annotations in your request/response models.

### snitch-tests

```kotlin
testImplementation("io.github.memoizr:snitch-tests:4.0.1")
```

Contains testing utilities and a fluent DSL for writing integration tests for Snitch services. Includes assertion helpers and logging configuration for tests.

### snitch-shank

```kotlin
implementation("io.github.memoizr:snitch-shank:4.0.1")
```

Integrates the Shank dependency injection library with Snitch, providing a lightweight, code-generated DI solution for your applications.

### snitch-exposed

```kotlin
implementation("io.github.memoizr:snitch-exposed:4.0.1")
```

Integrates the Exposed SQL library with Snitch, providing type-safe database access with automatic object mapping and transaction management.

### snitch-exposed-h2

```kotlin
implementation("io.github.memoizr:snitch-exposed-h2:4.0.1")
```

Provides specialized support for H2 databases with Snitch and Exposed, ideal for development and testing environments.

### snitch-exposed-postgres

```kotlin
implementation("io.github.memoizr:snitch-exposed-postgres:4.0.1")
```

Provides specialized support for PostgreSQL databases with Snitch and Exposed, suitable for production environments.

### snitch-kofix

```kotlin
testImplementation("io.github.memoizr:snitch-kofix:4.0.1")
```

Testing utilities for property-based testing and mocking in Snitch applications.

## Choosing the Right Dependencies

For most applications, the bootstrap module is sufficient:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:4.0.1")
}
```

For applications requiring coroutines:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:4.0.1")
    implementation("io.github.memoizr:snitch-coroutines:4.0.1")
}
```

For applications with database access:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:4.0.1")
    implementation("io.github.memoizr:snitch-exposed:4.0.1")
    implementation("io.github.memoizr:snitch-exposed-postgres:4.0.1") // For production
    implementation("io.github.memoizr:snitch-exposed-h2:4.0.1") // For testing
}
```

For applications requiring authentication:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:4.0.1")
    implementation("io.github.memoizr:snitch-auth:4.0.1")
}
```

For a more customized setup using a different JSON library or web server, you can use just the core module and add your own implementations:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-core:4.0.1")
    // Add your preferred JSON parser and server
}
```

## Artifact Breakdown

| Artifact | Purpose | Key Features |
|----------|---------|--------------|
| snitch-bootstrap | Complete starter package | All-in-one solution for getting started quickly |
| snitch-core | Core framework | Route definitions, handlers, parameter validation, middleware |
| snitch-types | Common types | Basic types used throughout the framework |
| snitch-undertow | Server implementation | Integrates with Undertow web server |
| snitch-gsonparser | JSON parsing | Handles JSON serialization/deserialization with Gson |
| snitch-coroutines | Async support | Kotlin Coroutines integration for asynchronous handlers |
| snitch-auth | Authentication | Authentication and authorization support |
| snitch-validation | Enhanced validation | Hibernate Validator integration |
| snitch-tests | Testing utilities | Testing DSL and assertion helpers |
| snitch-shank | Dependency injection | Lightweight DI integration |
| snitch-exposed | Database access | Core database integration with Exposed |
| snitch-exposed-h2 | H2 database | H2 database support for development and testing |
| snitch-exposed-postgres | PostgreSQL database | PostgreSQL support for production use |
| snitch-kofix | Testing utilities | Property-based testing and mocking |