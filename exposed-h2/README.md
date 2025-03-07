# Snitch Exposed H2 Module

This module provides integration between the Snitch framework and the H2 database via the Exposed SQL library for Kotlin. It offers convenient utilities for connecting to H2 databases, suitable for both testing and production environments.

## Features

- **Simple configuration** for H2 database connections
- **In-memory database support** ideal for testing
- **File-based database support** for persistent storage
- **Built on Snitch Exposed** core module

## Installation

Add the module to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-exposed-h2:$snitchVersion")
}
```

## Usage

### Creating H2 Database Connections

```kotlin
import snitch.exposed.h2.h2ConnectionConfig
import snitch.exposed.ExposedModule

// In-memory database (default configuration)
val config = h2ConnectionConfig()
ExposedModule.connection(config)

// In-memory database with custom name
val namedConfig = h2ConnectionConfig(name = "testdb")
ExposedModule.connection(namedConfig)

// File-based database
val fileConfig = h2ConnectionConfig(
    name = "myapp",
    inMemory = false,
    directory = "/path/to/data"
)
ExposedModule.connection(fileConfig)

// Custom configuration
val customConfig = h2ConnectionConfig(
    url = "jdbc:h2:~/custom;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE",
    user = "sa",
    password = "password"
)
ExposedModule.connection(customConfig)
```

### Using H2 for Testing

H2 is particularly useful for integration tests:

```kotlin
// Test setup
@BeforeEach
fun setupDatabase() {
    // Create a fresh in-memory database for each test
    val config = h2ConnectionConfig(name = "test_${UUID.randomUUID()}")
    ExposedModule.connection(config)
    
    transaction {
        SchemaUtils.create(TestTable)
    }
}

@AfterEach
fun cleanupDatabase() {
    // Clean up after each test
    transaction {
        SchemaUtils.drop(TestTable)
    }
}
```

## Configuration Options

The `h2ConnectionConfig` function provides several options for configuring your H2 database:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `name` | String | "test" | Database name |
| `inMemory` | Boolean | true | Whether to use in-memory storage |
| `directory` | String? | null | Directory for file-based storage |
| `url` | String | auto-generated | Custom JDBC URL (overrides other options) |
| `user` | String | "sa" | Database username |
| `password` | String | "" | Database password |

## Best Practices

1. **Testing**: Use in-memory databases for tests to ensure test isolation and fast execution.
2. **Development**: Use file-based databases during development for persistence between runs.
3. **Memory Management**: Set appropriate database closing behavior based on your needs:
   - For tests, use `DB_CLOSE_DELAY=-1` to keep the database in memory until the JVM exits.
   - For production, configure appropriate cleanup settings.

## Related Modules

- [Snitch Exposed](../exposed/README.md) - Core database functionality
- [Snitch Exposed Postgres](../exposed-postgres/README.md) - PostgreSQL integration

## License

This module is licensed under the same license as the Snitch framework.