# Snitch Exposed PostgreSQL Module

This module provides integration between the Snitch framework and PostgreSQL databases via the Exposed SQL library for Kotlin. It offers convenient utilities for connecting to PostgreSQL databases in a type-safe way.

## Features

- **Simple configuration** for PostgreSQL database connections
- **Connection pool support** for efficient database access
- **Connection properties management** for advanced PostgreSQL options
- **Built on Snitch Exposed** core module

## Installation

Add the module to your `build.gradle.kts` file:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-exposed-postgres:$snitchVersion")
}
```

## Usage

### Creating PostgreSQL Database Connections

```kotlin
import snitch.exposed.postgres.postgresConnectionConfig
import snitch.exposed.ExposedModule

// Basic connection
val config = postgresConnectionConfig(
    host = "localhost",
    port = 5432,
    database = "myapp",
    user = "postgres",
    password = "password"
)
ExposedModule.connection(config)

// Using a custom URL
val customConfig = postgresConnectionConfig(
    url = "jdbc:postgresql://db.example.com:5432/myapp",
    user = "appuser",
    password = "apppassword"
)
ExposedModule.connection(customConfig)

// With additional connection properties
val advancedConfig = postgresConnectionConfig(
    host = "localhost",
    database = "myapp",
    user = "postgres",
    password = "password",
    properties = mapOf(
        "ssl" to "true",
        "sslmode" to "require",
        "sslfactory" to "org.postgresql.ssl.NonValidatingFactory",
        "tcpKeepAlive" to "true"
    )
)
ExposedModule.connection(advancedConfig)
```

### Connection Pooling

The module supports HikariCP connection pooling out of the box:

```kotlin
import snitch.exposed.postgres.postgresConnectionConfig
import snitch.exposed.ExposedModule
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

// Create a connection pool
val hikariConfig = HikariConfig().apply {
    jdbcUrl = "jdbc:postgresql://localhost:5432/myapp"
    username = "postgres"
    password = "password"
    maximumPoolSize = 10
    isAutoCommit = false
    transactionIsolation = "TRANSACTION_REPEATABLE_READ"
}
val dataSource = HikariDataSource(hikariConfig)

// Use the data source with Exposed
val config = postgresConnectionConfig(
    dataSource = dataSource
)
ExposedModule.connection(config)
```

## Configuration Options

The `postgresConnectionConfig` function provides several options for configuring your PostgreSQL database connection:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `host` | String | "localhost" | Database host |
| `port` | Int | 5432 | Database port |
| `database` | String | required | Database name |
| `user` | String | required | Database username |
| `password` | String | required | Database password |
| `url` | String | auto-generated | Custom JDBC URL (overrides host/port/database) |
| `properties` | Map<String, String> | empty | Additional JDBC connection properties |
| `dataSource` | DataSource | null | Custom data source (overrides other connection options) |

## Best Practices

1. **Connection Pooling**: Use connection pooling in production for better performance.
2. **SSL**: Enable SSL in production environments for secure database connections.
3. **Transactions**: Always use transactions for database operations to ensure data consistency.
4. **Error Handling**: Implement proper error handling and retry mechanisms for database operations.
5. **Connection Management**: Close connections properly by shutting down the connection pool when your application stops.

## PostgreSQL-Specific Features

When working with PostgreSQL, you can leverage specific features:

```kotlin
// Using PostgreSQL-specific column types
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val metadata = jsonb("metadata") // PostgreSQL JSONB type
    val searchVector = tsvector("search_vector") // Full-text search
    
    override val primaryKey = PrimaryKey(id)
}

// Using PostgreSQL functions in queries
transaction {
    Users.select {
        Users.metadata.jsonPathExists("$.preferences.darkMode")
    }
}
```

## Related Modules

- [Snitch Exposed](../exposed/README.md) - Core database functionality
- [Snitch Exposed H2](../exposed-h2/README.md) - H2 database integration (useful for testing)

## License

This module is licensed under the same license as the Snitch framework.