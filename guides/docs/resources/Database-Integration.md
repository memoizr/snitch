# Database Integration with Snitch Exposed

Snitch offers first-class database integration through its Exposed modules, providing a seamless, type-safe experience for working with relational databases in your applications. This guide explores how Snitch's Exposed modules help you integrate databases into your applications with minimal boilerplate and maximum type safety.

## Introduction

Working with databases in web applications is often fraught with challenges:

- **Type safety** is frequently lost at the database boundary
- **Object-relational impedance mismatch** requires tedious mapping code
- **Transaction management** adds complexity to request handling
- **Database schema evolution** creates maintenance challenges

Snitch solves these problems through a powerful integration with the [Exposed](https://github.com/JetBrains/Exposed) SQL library for Kotlin. This integration provides type-safe database access with automatic object mapping and transaction management tailored specifically for Snitch applications.

## Available Modules

Snitch provides three modules for database integration:

1. **exposed**: Core module with base abstractions, mapping utilities, and transaction management
2. **exposed-h2**: Specialized module for H2 database connections, ideal for testing and development
3. **exposed-postgres**: Specialized module for PostgreSQL database connections, suitable for production

## Key Benefits

### 1. Type-Safe Database Access

Snitch Exposed modules leverage Kotlin's type system to provide compile-time safety for database operations:

```kotlin
// Type-safe table definition
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 255)
    
    override val primaryKey = PrimaryKey(id)
}

// Type-safe query
val user = transaction {
    Users.findOne<UserModel> { Users.email eq "user@example.com" }
}
```

The compiler ensures that:
- Your queries reference existing columns
- Your comparisons use the correct data types
- Your result mappings align with your domain models

### 2. Automatic Object Mapping

One of the most powerful features of Snitch Exposed is its automatic object mapping. This eliminates the tedious and error-prone code typically required to map between database tables and domain objects:

```kotlin
// Domain model
data class User(val name: String, val email: String)
data class UserWithId(val id: Int, val name: String, val email: String)

// Automatic mapping from database to domain model
val users = transaction {
    Users.findAll<UserWithId>()
}

// Automatic mapping from domain model to database
val user = User("John Doe", "john@example.com")
transaction {
    Users.insert(user)
}
```

Behind the scenes, Snitch:
1. Inspects the properties of your domain model using Kotlin reflection
2. Matches them with database column names
3. Performs appropriate type conversions
4. Handles value classes and nested objects
5. Supports custom mapping for complex cases

This eliminates thousands of lines of boilerplate code in typical applications while maintaining type safety.

### 3. Integrated Transaction Management

Snitch Exposed seamlessly integrates transaction management with the routing system:

```kotlin
withTransaction {
    POST("users") with body<User>() isHandledBy {
        val user = body
        val id = Users.insert(user)[Users.id]
        UserResponse(id, user.name, user.email).ok
    }
}
```

This provides several benefits:
- **Automatic transaction boundaries** around route handlers
- **Consistent error handling** with automatic rollback on exceptions
- **Clean code organization** without explicit transaction blocks

### 4. Value Class Support

Modern Kotlin applications often use value classes for type safety. Snitch Exposed provides first-class support for value classes:

```kotlin
// Domain model with value classes
@JvmInline
value class UserId(val value: Int)

data class UserWithValueId(val id: UserId, val name: String, val email: String)

// Automatic mapping works with value classes
val user = transaction {
    Users.findOne<UserWithValueId> { Users.id eq 1 }
}

println(user.id.value) // Accesses the wrapped value
```

This enables more type-safe domain models without sacrificing database integration.

### 5. Flexible Schema Management

Snitch Exposed provides tools for schema management:

```kotlin
// Create tables
transaction {
    SchemaUtils.create(Users, Posts, Comments)
}

// Add missing columns during updates
database.addMissingColumns(Users)

// Drop tables
transaction {
    SchemaUtils.drop(Users)
}
```

Combined with migration tools, this gives you flexible options for evolving your database schema alongside your application.

## Database-Specific Modules

### H2 Integration

The `exposed-h2` module provides specialized support for H2 databases, which are particularly useful for:

- **Development environments** where quick setup is important
- **Testing** where database isolation is critical
- **Embedded applications** that need a lightweight database

Key features include:
- In-memory database support with zero configuration
- File-based database options for persistence
- Automatic schema creation and teardown

Example:

```kotlin
// Quick in-memory database for testing
val config = h2ConnectionConfig()
ExposedModule.connection(config)

// File-based database for development
val devConfig = h2ConnectionConfig(
    name = "myapp",
    inMemory = false,
    directory = "./data"
)
ExposedModule.connection(devConfig)
```

### PostgreSQL Integration

The `exposed-postgres` module provides specialized support for PostgreSQL databases, which are ideal for:

- **Production environments** requiring advanced database features
- **High-performance applications** needing robust concurrency
- **Data-intensive applications** leveraging PostgreSQL's rich data types

Key features include:
- Connection pooling for efficient resource use
- SSL configuration for secure connections
- Support for PostgreSQL-specific data types
- Advanced query capabilities

Example:

```kotlin
// Basic PostgreSQL connection
val config = postgresConnectionConfig(
    host = "localhost",
    database = "myapp",
    user = "appuser",
    password = "password"
)
ExposedModule.connection(config)

// With connection pooling
val pooledConfig = postgresConnectionConfig(
    dataSource = createHikariDataSource() // Your connection pool
)
ExposedModule.connection(pooledConfig)
```

## Advanced Usage Patterns

### Custom Type Mapping

For complex mapping scenarios, Snitch Exposed provides a custom mapping facility:

```kotlin
// Define custom mapping between types
AutoMapper.customMapping<User, UserDTO>(
    from = { user -> UserDTO(user.name, user.email, "Additional info") },
    to = { dto -> User(dto.name, dto.email) }
)

// Now you can convert between them
val user = User("John", "john@example.com")
val dto = user.to<UserDTO>()
```

This is particularly useful for:
- Converting between persistence models and API models
- Handling legacy database schemas
- Supporting complex business logic during mapping

### Transaction Strategies

Snitch Exposed supports different transaction strategies to match your application's needs:

```kotlin
// Explicit transactions for fine-grained control
POST("users") isHandledBy {
    val user = body
    transaction {
        // Database operations
    }
    "User created".ok
}

// Transaction-per-request for comprehensive coverage
withTransaction {
    POST("users") with body<User>() isHandledBy {
        // Everything here runs within a transaction
    }
    
    GET("users") isHandledBy {
        // This handler also runs in a transaction
    }
}
```

### Working with Relationships

Snitch Exposed makes it easy to work with database relationships:

```kotlin
// Table definitions
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    
    override val primaryKey = PrimaryKey(id)
}

object Posts : Table("posts") {
    val id = integer("id").autoIncrement()
    val userId = integer("user_id") references Users.id
    val title = varchar("title", 255)
    val content = text("content")
    
    override val primaryKey = PrimaryKey(id)
}

// Domain models
data class User(val id: Int, val name: String)
data class Post(val id: Int, val userId: Int, val title: String, val content: String)
data class UserWithPosts(val id: Int, val name: String, val posts: List<Post>)

// Query with relationships
val usersWithPosts = transaction {
    Users.findAll<User>().map { user ->
        val posts = Posts.findAll<Post> { Posts.userId eq user.id }
        UserWithPosts(user.id, user.name, posts)
    }
}
```

## Performance Considerations

Snitch Exposed is designed for performance:

1. **Connection pooling** reuses database connections efficiently
2. **Batch operations** minimize database roundtrips
3. **Lazy loading** defers expensive operations until needed
4. **Caching** reduces redundant database queries

For maximum performance:

```kotlin
// Batch inserts for bulk operations
transaction {
    Users.batchInsert(userList) {
        it[name] = it.name
        it[email] = it.email
    }
}

// Targeted queries to minimize data transfer
val userCount = transaction {
    Users.selectAll().count()
} 

// Using database-specific optimizations
transaction {
    // PostgreSQL JSON functions for complex data
    Users.select { Users.metadata.jsonPathExists("$.preferences") }
}
```

## Integration with Snitch Features

Snitch Exposed integrates seamlessly with other Snitch features:

### Validation Integration

```kotlin
withTransaction {
    POST("users") with validate<User>() isHandledBy {
        // Validation runs before the transaction begins
        // Database operations only happen for valid requests
        val user = body
        Users.insert(user)
        "User created".ok
    }
}
```

### Error Handling Integration

```kotlin
withTransaction {
    POST("users") with body<User>() isHandledBy {
        try {
            val user = body
            Users.insert(user)
            "User created".ok
        } catch (e: Exception) {
            // Transaction automatically rolls back
            "Error: ${e.message}".serverError()
        }
    }
}
```

### Testing Integration

```kotlin
// Test setup with in-memory database
@BeforeEach
fun setup() {
    val config = h2ConnectionConfig()
    ExposedModule.connection(config)
    
    transaction {
        SchemaUtils.create(TestTable)
    }
}

// Test endpoint with transaction
@Test
fun `test user creation`() {
    given {
        withTransaction {
            POST("users") with body<User>() isHandledBy {
                val user = body
                Users.insert(user)
                "User created".ok
            }
        }
    } then {
        POST("/users") withBody User("Test", "test@example.com") expect {
            expect that it.statusCode() isEqualTo 200
            
            // Verify database state
            transaction {
                val count = Users.selectAll().count()
                expect that count isEqualTo 1
            }
        }
    }
}
```

## Best Practices

### 1. Domain Model Separation

Keep your domain models separate from your database entities:

```kotlin
// Database table
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val email = varchar("email", 255)
    
    override val primaryKey = PrimaryKey(id)
}

// Domain model (persistence)
data class UserEntity(val id: Int, val name: String, val email: String)

// Domain model (business logic)
data class User(val id: Int, val name: String, val email: String, val isValid: Boolean)

// API model
data class UserResponse(val id: Int, val name: String)
```

This separation allows each model to evolve independently according to its concerns.

### 2. Repository Pattern

Encapsulate database access in repository classes:

```kotlin
class UserRepository {
    fun findById(id: Int): User? = transaction {
        Users.findOneOrNull<UserEntity> { Users.id eq id }
            ?.let { mapToUser(it) }
    }
    
    fun create(user: User): Int = transaction {
        val entity = mapToEntity(user)
        Users.insert(entity)[Users.id]
    }
    
    private fun mapToUser(entity: UserEntity): User = // mapping logic
    private fun mapToEntity(user: User): UserEntity = // mapping logic
}
```

This approach:
- Centralizes database access logic
- Makes database operations more testable
- Provides a clean API for business logic

### 3. Transaction Management

Choose the right transaction boundaries:

```kotlin
// Transaction per request (most common)
withTransaction {
    // Multiple endpoint handlers here
}

// Transaction per operation (more granular)
POST("users") isHandledBy {
    transaction {
        // Single operation
    }
}

// Transaction per unit of work (intermediate)
POST("complex-operation") isHandledBy {
    transaction {
        // Multiple related operations
        // Either all succeed or all fail
    }
}
```

### 4. Error Handling

Implement robust error handling:

```kotlin
withTransaction {
    POST("users") with body<User>() isHandledBy {
        try {
            // Database operations
        } catch (e: Exception) {
            logger.error("Database error", e)
            when (e) {
                is UniqueConstraintException -> "User already exists".conflict()
                is ReferenceConstraintException -> "Referenced entity not found".badRequest()
                else -> "Internal server error".serverError()
            }
        }
    }
}
```

### 5. Testing Strategy

Adopt a comprehensive testing strategy:

- **Unit tests**: Test your repositories in isolation
- **Integration tests**: Test endpoints with an in-memory H2 database
- **Schema tests**: Verify your schema changes are compatible
- **Performance tests**: Validate database performance under load

## Conclusion

Snitch Exposed offers a powerful, type-safe approach to database integration that eliminates boilerplate while maintaining the flexibility and performance needed for real-world applications. By leveraging Kotlin's type system and reflection capabilities, it provides a seamless bridge between your domain models and database schema.

Whether you're building a simple application with H2 or a complex system with PostgreSQL, Snitch Exposed gives you the tools to work with databases efficiently and confidently.

## Further Reading

- [Exposed Documentation](https://github.com/JetBrains/Exposed/wiki)
- [H2 Database Engine](https://h2database.com/html/main.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)