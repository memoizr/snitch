# Snitch Artifacts

Snitch is modular by design, providing several artifacts that can be used independently based on your needs. All artifacts are published to Maven Central with the prefix `io.github.memoizr:snitch-{module-name}`.

## Core Artifacts

### snitch-bootstrap

```kotlin
implementation("io.github.memoizr:snitch-bootstrap:1.0.0")
```

This is the main artifact most users should depend on. It includes everything needed to create a complete web service using Snitch with Undertow as the server and Gson for JSON parsing.

**Dependencies**: Includes core, gsonparser, and undertow modules.

### snitch-core

```kotlin
implementation("io.github.memoizr:snitch-core:1.0.0")
```

The core module contains the essential building blocks of Snitch:
- DSL for defining routes and handlers
- Parameter definition and validation framework
- Middleware and conditions system
- Documentation generation engine

This module is server-agnostic and doesn't include any specific JSON parsing implementation.

### snitch-types

```kotlin
implementation("io.github.memoizr:snitch-types:1.0.0")
```

A lightweight module containing the basic types and interfaces used across the Snitch ecosystem. This module has minimal dependencies and can be used in your domain model to avoid pulling in the entire Snitch framework.

## Extensions and Implementations

### snitch-undertow

```kotlin
implementation("io.github.memoizr:snitch-undertow:1.0.0")
```

Provides Undertow server integration for Snitch. Undertow is a flexible, high-performance web server by JBoss that serves as the default server implementation for Snitch.

### snitch-gsonparser

```kotlin
implementation("io.github.memoizr:snitch-gsonparser:1.0.0")
```

Implements JSON parsing and serialization using Google's Gson library. This module allows Snitch to convert between JSON and Kotlin objects.

### snitch-coroutines

```kotlin
implementation("io.github.memoizr:snitch-coroutines:1.0.0")
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

### snitch-validation

```kotlin
implementation("io.github.memoizr:snitch-validation:1.0.0")
```

Provides integration with Hibernate Validator (Jakarta Bean Validation), allowing you to use standard validation annotations in your request/response models.

### snitch-tests

```kotlin
testImplementation("io.github.memoizr:snitch-tests:1.0.0")
```

Contains testing utilities and a fluent DSL for writing integration tests for Snitch services. Includes assertion helpers and logging configuration for tests.

## Choosing the Right Dependencies

For most applications, the bootstrap module is sufficient:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:1.0.0")
}
```

For applications requiring coroutines:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:1.0.0")
    implementation("io.github.memoizr:snitch-coroutines:1.0.0")
}
```

For advanced validation with Hibernate Validator:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:1.0.0")
    implementation("io.github.memoizr:snitch-validation:1.0.0")
}
```

For a more customized setup using a different JSON library or web server, you can use just the core module and add your own implementations:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-core:1.0.0")
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
| snitch-validation | Enhanced validation | Hibernate Validator integration |
| snitch-tests | Testing utilities | Testing DSL and assertion helpers |