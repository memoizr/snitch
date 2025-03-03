# Snitch Exposed Module

The Snitch Exposed module provides seamless integration between the [Snitch](https://github.com/memoizr/snitch) framework and the [Exposed](https://github.com/JetBrains/Exposed) SQL library for Kotlin. This integration enables type-safe database access with automatic object mapping and transaction management for your Snitch applications.

## Features

- **Type-safe database access** using Exposed SQL DSL
- **Seamless object mapping** between domain models and database tables
- **Transaction management** integrated with Snitch's routing system
- **Database-specific modules** for H2 and PostgreSQL
- **Value class support** for domain-driven design
- **Custom type mapping** for advanced use cases

## Installation

Add the desired modules to your `build.gradle.kts` file:

```kotlin
dependencies {
    // Core Exposed integration
    implementation("io.github.memoizr:snitch-exposed:$snitchVersion")
    
    // Database-specific drivers (choose one or both)
    implementation("io.github.memoizr:snitch-exposed-h2:$snitchVersion")      // For H2 database
    implementation("io.github.memoizr:snitch-exposed-postgres:$snitchVersion") // For PostgreSQL
}
```

## Modules

The Exposed integration consists of three main modules:

1. **exposed**: Core module with base abstractions, mapping utilities, and transaction management.
2. **exposed-h2**: Specialized module for H2 database connections.
3. **exposed-postgres**: Specialized module for PostgreSQL database connections.

## Getting Started

### Database Connection

Configure and establish a database connection:

```kotlin
// Using H2 (in-memory database)
val config = h2ConnectionConfig()
ExposedModule.connection(config)

// Using PostgreSQL
val config = postgresConnectionConfig(
    url = "jdbc:postgresql://localhost:5432/mydb",
    user = "dbuser",
    password = "dbpassword"
)
ExposedModule.connection(config)

// Custom configuration
val config = DatabaseConnectionConfig(
    url = "jdbc:mysql://localhost:3306/mydb",
    driver = "com.mysql.cj.jdbc.Driver",
    user = "root",
    password = "password"
)
ExposedModule.connection(config)
```

### Defining Database Tables

Define your database tables using Exposed's DSL:

```kotlin
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 255)
    val createdAt = datetime("created_at")
    
    override val primaryKey = PrimaryKey(id)
}
```

### Creating Schema

Create database tables:

```kotlin
// Using SchemaUtils
transaction {
    SchemaUtils.create(Users)
}

// Or using ExposedDatabase
val database = ExposedDatabase(connection, Users)
database.createSchema()

// Add missing columns during schema updates
database.addMissingColumns(Users)
```

### Domain Models

Define domain models that correspond to your database tables:

```kotlin
// Simple domain model
data class User(
    val name: String,
    val email: String
)

// Domain model with ID
data class UserWithId(
    val id: Int,
    val name: String,
    val email: String
)

// Using value classes
@JvmInline
value class UserId(val value: Int)

data class UserWithValueId(
    val id: UserId,
    val name: String,
    val email: String
)
```

### Using Transactions in Routes

Wrap your route handlers in transactions:

```kotlin
withTransaction {
    // This route handler will be executed in a transaction
    POST("users") with body<User>() isHandledBy {
        val user = body
        val id = Users.insert(user)[Users.id]
        UserResponse(id, user.name, user.email).ok
    }
    
    // Transaction is automatically rolled back on exceptions
    POST("users-with-validation") with body<User>() isHandledBy {
        val user = body
        if (!isValidEmail(user.email)) {
            throw RuntimeException("Invalid email")
        }
        val id = Users.insert(user)[Users.id]
        UserResponse(id, user.name, user.email).ok
    }
}
```

### Querying Data

Query data with automatic mapping to domain objects:

```kotlin
// Find all users
val allUsers = transaction {
    Users.findAll<UserWithId>()
}

// Find a specific user by ID
val user = transaction {
    Users.findOne<UserWithId> { Users.id eq userId }
}

// Find a user that might not exist
val maybeUser = transaction {
    Users.findOneOrNull<UserWithId> { Users.email eq "user@example.com" }
}
```

### Creating and Updating Data

Insert and update data with automatic mapping:

```kotlin
// Insert a single user
val insertResult = transaction {
    Users.insert(User("John Doe", "john@example.com"))
}
val userId = insertResult[Users.id]

// Batch insert multiple users
val users = listOf(
    User("Alice", "alice@example.com"),
    User("Bob", "bob@example.com")
)
transaction {
    Users.batchInsert(users)
}

// Update a user
val updatedUser = UserWithId(1, "Updated Name", "updated@example.com")
transaction {
    Users.updateWhere(updatedUser) { Users.id eq updatedUser.id }
}
```

## Advanced Features

### Custom Type Mapping

Define custom mappings between types:

```kotlin
// Map User to UserDTO and back
AutoMapper.customMapping<User, UserDTO>(
    from = { user -> UserDTO(user.name, user.email, "Default") },
    to = { dto -> User(dto.name, dto.email) }
)

// Now you can convert between them
val user = User("John", "john@example.com")
val dto = user.to<UserDTO>()
val backToUser = dto.to<User>()
```

### Working with Value Classes

Map between regular types and value classes:

```kotlin
// Domain model with value class
data class UserWithValueId(val id: UserId, val name: String, val email: String)

// Unwrap a value class to its underlying type
val userId = UserId(42)
val rawId = userId.unwrap() // Returns 42

// Wrap a value in a value class
val wrappedId = 42.wrap(UserId::class) as UserId
```

## Examples

### Complete Example

Here's a complete example showing how to use the Exposed module with Snitch:

```kotlin
import org.jetbrains.exposed.sql.Table
import snitch.exposed.*

// Database table definition
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 255)
    
    override val primaryKey = PrimaryKey(id)
}

// Domain models
data class User(val name: String, val email: String)
data class UserWithId(val id: Int, val name: String, val email: String)
data class UserResponse(val id: Int, val name: String, val email: String)

// Initialize database connection
fun initDatabase() {
    val config = h2ConnectionConfig() // For testing with H2
    ExposedModule.connection(config)
    
    transaction {
        SchemaUtils.create(Users)
    }
}

// Define routes with transaction support
val routes = routes {
    withTransaction {
        POST("users") with body<User>() isHandledBy {
            val user = body
            val id = Users.insert(user)[Users.id]
            UserResponse(id, user.name, user.email).ok
        }
        
        GET("users") isHandledBy {
            val users = Users.findAll<UserWithId>()
            users.ok
        }
        
        GET("users" / id) isHandledBy {
            val userId = request[id]
            val user = Users.findOneOrNull<UserWithId> { Users.id eq userId }
            user?.ok ?: "User not found".notFound()
        }
    }
}
```

## Database-Specific Modules

### exposed-h2

The H2 module provides utilities for working with H2 databases:

```kotlin
// Create in-memory H2 database configuration
val config = h2ConnectionConfig()

// Create file-based H2 database configuration
val fileDbConfig = h2ConnectionConfig(
    name = "mydb",
    inMemory = false,
    directory = "/path/to/db"
)
```

### exposed-postgres

The PostgreSQL module provides utilities for working with PostgreSQL databases:

```kotlin
// Create PostgreSQL database configuration
val config = postgresConnectionConfig(
    host = "localhost",
    port = 5432,
    database = "mydb",
    user = "dbuser",
    password = "dbpassword"
)

// Configure connection properties
val customConfig = postgresConnectionConfig(
    url = "jdbc:postgresql://custom-host:5433/mydb",
    user = "dbuser",
    password = "dbpassword",
    properties = mapOf(
        "ssl" to "true",
        "sslmode" to "require"
    )
)
```

## Best Practices

1. **Use transactions:** Always use transactions for database operations to ensure data consistency.
2. **Domain model separation:** Keep your domain models separate from your database entities for better maintainability.
3. **Connection management:** Initialize the database connection at application startup and reuse it.
4. **Error handling:** Use appropriate error handling strategies for database operations, especially in production environments.
5. **Testing:** Use the H2 in-memory database for unit and integration tests.

## Related Documentation

- [Exposed documentation](https://github.com/JetBrains/Exposed/wiki)
- [Snitch documentation](https://github.com/memoizr/snitch)

## License

This module is licensed under the same license as the Snitch framework.