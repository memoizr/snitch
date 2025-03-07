# In-Depth: Database Integration with Exposed

This document provides an in-depth exploration of Snitch's integration with the Exposed SQL library, focusing on the technical aspects and internal workings of the implementation.

## Architecture Overview

Snitch's Exposed integration consists of three main modules:

1. **exposed**: Core module with the foundational abstractions and utilities
2. **exposed-h2**: H2 database adapter for testing and development
3. **exposed-postgres**: PostgreSQL adapter for production use

The architecture follows these design principles:

- **Type safety**: Leveraging Kotlin's type system for database operations
- **Automatic mapping**: Using reflection to minimize boilerplate
- **Transaction management**: Integrating with Snitch's routing system
- **Database agnosticism**: Core abstractions work with any supported database

## Core Components

### DatabaseConnectionConfig

The `DatabaseConnectionConfig` class serves as the primary configuration point:

```kotlin
data class DatabaseConnectionConfig(
    val url: String,
    val driver: String,
    val user: String,
    val password: String,
    val setupConnection: ((Database) -> Unit)? = null,
    val databaseConfig: DatabaseConfig? = null,
    val manager: (Database) -> TransactionManager = { db -> TransactionManager.manager.defaultDatabase = db }
)
```

This configuration is used to establish a database connection and configure transaction management.

### ExposedModule

The `ExposedModule` provides dependency injection support via Shank:

```kotlin
object ExposedModule {
    private val connectionProvider = single<DatabaseConnectionConfig, Database> { config ->
        Database.connect(
            url = config.url,
            driver = config.driver,
            user = config.user,
            password = config.password,
            databaseConfig = config.databaseConfig
        ).also {
            config.setupConnection?.invoke(it)
            config.manager(it)
        }
    }

    fun connection(config: DatabaseConnectionConfig): Database = connectionProvider(config)
}
```

This creates a singleton database connection that can be shared throughout your application.

### Transaction Management

The `withTransaction` decorator integrates transactions with route handlers:

```kotlin
fun Routes.withTransaction(builder: TransactionalRoutes.() -> Unit) {
    val routes = TransactionalRoutes().apply(builder)
    routes.endpoints.forEach { addEndpoint(it) }
}

class TransactionalRoutes : Routes() {
    override fun <T : Any> addEndpoint(endpoint: Endpoint<T>) {
        val wrappedHandler = { request: Request<T> ->
            transaction {
                endpoint.handler(request)
            }
        }
        super.addEndpoint(endpoint.copy(handler = wrappedHandler))
    }
}
```

This approach ensures that:
1. Each route handler executes within a transaction
2. Exceptions trigger automatic rollback
3. Successful execution commits the transaction

### Object Mapping

The core of the automatic mapping system resides in the `Mapping.kt` file, which provides:

1. **Type Conversion**: Converting between domain models and database entities
2. **Property Mapping**: Matching properties by name between objects
3. **Value Class Handling**: Supporting Kotlin's value classes
4. **Custom Mapping**: Allowing user-defined conversions

The mapping system uses Kotlin reflection to:
- Inspect property names and types
- Match database columns to model properties
- Perform appropriate type conversions
- Handle nested objects and collections

Key extension functions:

```kotlin
// Convert between similar objects
inline fun <reified R : Any> Any.to(): R

// Map ResultRow to domain model
fun <T : ColumnSet, R : Any> ResultRow.to(from: T, to: KClass<R>): R

// Insert domain model into table
fun <R : Any> Table.insert(value: R, customize: Table.(UpdateBuilder<*>) -> Unit = {}): InsertStatement<Number>

// Find all entities and map to domain model
inline fun <reified R : Any> Table.findAll(e: SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE })
```

### Schema Management

The `ExposedDatabase` class provides utilities for schema management:

```kotlin
class ExposedDatabase(
    private val connection: Database,
    private vararg val tables: Table
) {
    fun createSchema() = transaction(connection) {
        SchemaUtils.create(*tables)
    }
    
    fun dropSchema() = transaction(connection) {
        SchemaUtils.drop(*tables)
    }
    
    fun addMissingColumns() = transaction(connection) {
        SchemaUtils.addMissingColumnsStatements(*tables)
    }
}
```

## Database-Specific Implementations

### H2 Integration

The `exposed-h2` module provides a simplified API for H2 database connections:

```kotlin
fun h2ConnectionConfig(
    name: String = "test",
    inMemory: Boolean = true,
    directory: String? = null,
    url: String = buildH2Url(name, inMemory, directory),
    user: String = "sa",
    password: String = ""
): DatabaseConnectionConfig {
    return DatabaseConnectionConfig(
        url = url,
        driver = "org.h2.Driver",
        user = user,
        password = password
    )
}

private fun buildH2Url(name: String, inMemory: Boolean, directory: String?): String {
    return if (inMemory) {
        "jdbc:h2:mem:$name;DB_CLOSE_DELAY=-1;"
    } else {
        "jdbc:h2:${directory ?: "~"}/$name;DB_CLOSE_DELAY=-1;"
    }
}
```

This provides sensible defaults for H2 databases, particularly useful for:
- Unit and integration testing with in-memory databases
- Development environments with quick setup
- Simple applications that don't need a separate database server

### PostgreSQL Integration

The `exposed-postgres` module supports PostgreSQL connections with advanced options:

```kotlin
fun postgresConnectionConfig(
    host: String = "localhost",
    port: Int = 5432,
    database: String,
    user: String,
    password: String,
    properties: Map<String, String> = emptyMap(),
    url: String = buildPostgresUrl(host, port, database, properties),
    dataSource: DataSource? = null
): DatabaseConnectionConfig {
    return if (dataSource != null) {
        DatabaseConnectionConfig(
            url = url,
            driver = "org.postgresql.Driver",
            user = user,
            password = password,
            setupConnection = { db ->
                (db as DatabaseImpl).config.apply {
                    this.dataSource = dataSource
                }
            }
        )
    } else {
        DatabaseConnectionConfig(
            url = url,
            driver = "org.postgresql.Driver",
            user = user,
            password = password
        )
    }
}
```

This supports:
- Standard PostgreSQL connections
- Connection pooling via HikariCP or other DataSource implementations
- Advanced connection properties for tuning performance and security

## Technical Deep Dive

### Automatic Mapping Implementation

The core mapping functionality uses Kotlin reflection to inspect classes and convert between them:

```kotlin
fun <R : Any> Any.to(target: KClass<R>): R {
    if (this::class == target) return this as R
    if (target.isValue) return mapValueClass(target, this) as R

    val members = this::class.members.toList().map { it.name to it }.toMap()
    val constructor = target.constructors.first()
    
    try {
        val args = constructor.parameters.map {
            val sourceValue = members[it.name]?.call(this)?.unwrap()
            val targetClass = it.type.classifier as KClass<*>

            it to if (sourceValue == null) {
                null
            } else {
                // Handle different mapping scenarios...
            }
        }
        
        val instance = constructor.callBy(
            args.toMap().filterNot { (!it.key.type.isMarkedNullable && it.value == null) }
        )
        return instance
    } catch (e: Exception) {
        println("Error mapping $this to ${target}")
        throw e
    }
}
```

This complex system handles various mapping scenarios:
1. Direct mapping of identical types
2. Value class mapping
3. Collection mapping (List/Set)
4. Custom mapping via the AutoMapper
5. Recursive mapping of nested objects

### Transaction Implementation

The transaction integration leverages Exposed's transaction API:

```kotlin
inline fun <T> transaction(
    statement: Transaction.() -> T
): T {
    return org.jetbrains.exposed.sql.transactions.transaction {
        try {
            statement()
        } catch (e: Exception) {
            rollback()
            throw e
        }
    }
}
```

When combined with Snitch's routing:

```kotlin
class TransactionalRoutes : Routes() {
    override fun <T : Any> addEndpoint(endpoint: Endpoint<T>) {
        val wrappedHandler = { request: Request<T> ->
            transaction {
                endpoint.handler(request)
            }
        }
        super.addEndpoint(endpoint.copy(handler = wrappedHandler))
    }
}
```

This ensures that:
1. Every request handler executes within a transaction
2. Exceptions trigger transaction rollback
3. Successful execution commits the transaction
4. The transaction spans the entire request handling process

### Value Class Support

Value classes receive special handling to maintain type safety:

```kotlin
private fun mapValueClass(targetClass: KClass<*>, sourceValue: Any) = try {
    val first = targetClass.constructors.first()
    val kClass = first.parameters.first().type.classifier as KClass<*>
    if (kClass != sourceValue::class) {
        (
            mapping[sourceValue::class]?.from?.invoke(sourceValue)
                ?: mapping[kClass]?.to?.invoke(sourceValue)
            )
            ?.wrap(targetClass)
    } else {
        first.call(sourceValue)
    }
} catch (e: Exception) {
    println("Error instantiating value $sourceValue to $targetClass")
    throw e
}

fun Any.unwrap() = if (this::class.isValue) this::class.members.first().call(this) else this
fun Any.wrap(target: KClass<*>) = if (target.isValue) target.constructors.first().call(this) else this
```

This allows transparent conversion between primitive types and their value class wrappers.

### Custom Type Mapping System

The `AutoMapper` provides a registry for custom type conversions:

```kotlin
object AutoMapper {
    val mapping = mutableMapOf<KClass<*>, Mapper<Any, Any>>()

    inline fun <reified FROM, TO> customMapping(noinline from: (FROM) -> TO, noinline to: (TO) -> FROM) {
        mapping[FROM::class] = Mapper(from, to) as Mapper<Any, Any>
    }
}

data class Mapper<FROM, TO>(val from: (FROM) -> TO, val to: (TO) -> FROM)
```

This registry is consulted during the mapping process to apply custom conversions when needed.

## Performance Considerations

### Connection Pooling

For production use, connection pooling is essential:

```kotlin
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

fun createConnectionPool(
    url: String,
    user: String,
    password: String,
    maxPoolSize: Int = 10
): DataSource {
    val config = HikariConfig().apply {
        jdbcUrl = url
        username = user
        this.password = password
        maximumPoolSize = maxPoolSize
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
    }
    return HikariDataSource(config)
}

// Use with PostgreSQL
val dataSource = createConnectionPool(
    url = "jdbc:postgresql://localhost:5432/myapp",
    user = "dbuser",
    password = "password"
)

val config = postgresConnectionConfig(
    url = "jdbc:postgresql://localhost:5432/myapp",
    user = "dbuser",
    password = "password",
    dataSource = dataSource
)
```

### Batch Operations

For bulk operations, batch processing is significantly more efficient:

```kotlin
// Individual inserts - slow for large datasets
userList.forEach { user ->
    Users.insert(user)
}

// Batch insert - much faster
Users.batchInsert(userList) { user ->
    this[Users.name] = user.name
    this[Users.email] = user.email
}
```

### Query Optimization

Strategic querying can dramatically improve performance:

```kotlin
// Inefficient: Loads all users then filters in memory
val users = transaction {
    Users.findAll<User>()
}.filter { it.age > 30 }

// Efficient: Filters at the database level
val users = transaction {
    Users.findAll<User> { Users.age greater 30 }
}

// Inefficient: N+1 query problem
val usersWithPosts = transaction {
    Users.findAll<User>().map { user ->
        val posts = Posts.findAll<Post> { Posts.userId eq user.id } // One query per user!
        UserWithPosts(user, posts)
    }
}

// More efficient: Join in a single query
val usersWithPosts = transaction {
    (Users innerJoin Posts)
        .select { Posts.userId eq Users.id }
        .groupBy { it[Users.id] }
        .map { (userId, rows) ->
            val user = Users.findOne<User> { Users.id eq userId }
            val posts = rows.map { Posts.to<Post>(it) }
            UserWithPosts(user, posts)
        }
}
```

## Integration with Testing

The Exposed modules are designed to integrate seamlessly with testing:

```kotlin
class UserRepositoryTest {
    private lateinit var repository: UserRepository
    
    @BeforeEach
    fun setup() {
        // Create a unique in-memory database for test isolation
        val config = h2ConnectionConfig(name = "test_${UUID.randomUUID()}")
        ExposedModule.connection(config)
        
        transaction {
            SchemaUtils.create(Users)
        }
        
        repository = UserRepository()
    }
    
    @Test
    fun `create user stores data correctly`() {
        // Arrange
        val user = User("Test User", "test@example.com")
        
        // Act
        val id = repository.createUser(user)
        
        // Assert
        val retrievedUser = repository.getUserById(id)
        assertEquals("Test User", retrievedUser?.name)
        assertEquals("test@example.com", retrievedUser?.email)
    }
    
    @AfterEach
    fun tearDown() {
        transaction {
            SchemaUtils.drop(Users)
        }
    }
}
```

This approach provides:
- Test isolation with unique databases
- Fast test execution with in-memory storage
- Realistic database behavior for integration tests

## Advanced Usage

### Custom Column Types

Exposed supports custom column types for complex data:

```kotlin
// Define a column type for storing JSONs
class JsonColumnType<T : Any>(
    private val klass: KClass<T>,
    private val objectMapper: ObjectMapper
) : ColumnType() {
    override fun sqlType(): String = "TEXT"
    
    override fun valueFromDB(value: Any): T = when(value) {
        is String -> objectMapper.readValue(value, klass.java)
        else -> error("Unexpected value: $value of ${value::class.qualifiedName}")
    }
    
    override fun notNullValueToDB(value: Any): String = objectMapper.writeValueAsString(value)
}

// Use it in a table
object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val preferences = registerColumn<UserPreferences>("preferences", JsonColumnType(UserPreferences::class, objectMapper))
    
    override val primaryKey = PrimaryKey(id)
}
```

### Database Migration Strategies

For evolving schemas, you can use the Exposed utilities:

```kotlin
class DatabaseMigration(
    private val connection: Database,
    private vararg val tables: Table
) {
    fun migrateSchema() = transaction(connection) {
        // Get statements to add missing columns
        val statements = SchemaUtils.addMissingColumnsStatements(*tables)
        
        // Execute each statement
        statements.forEach { statement ->
            exec(statement)
        }
        
        // Check for any table structure changes
        for (table in tables) {
            val existingIndices = exec("SHOW INDEX FROM ${table.tableName}") { rs ->
                val indices = mutableListOf<String>()
                while (rs.next()) {
                    indices.add(rs.getString("Column_name"))
                }
                indices
            } ?: emptyList()
            
            // Create any missing indices
            table.indices.forEach { index ->
                val columns = index.columns.joinToString(", ") { it.name }
                if (!existingIndices.contains(columns)) {
                    val indexType = if (index.unique) "UNIQUE" else ""
                    exec("CREATE $indexType INDEX idx_${table.tableName}_${index.name} ON ${table.tableName}($columns)")
                }
            }
        }
    }
}
```

### Repository Pattern Implementation

A clean repository implementation with Snitch Exposed:

```kotlin
interface UserRepository {
    fun findById(id: Int): User?
    fun findAll(): List<User>
    fun create(user: User): Int
    fun update(user: User): Boolean
    fun delete(id: Int): Boolean
}

class ExposedUserRepository : UserRepository {
    override fun findById(id: Int): User? = transaction {
        Users.findOneOrNull<UserEntity> { Users.id eq id }
            ?.let { mapToDomain(it) }
    }
    
    override fun findAll(): List<User> = transaction {
        Users.findAll<UserEntity>()
            .map { mapToDomain(it) }
    }
    
    override fun create(user: User): Int = transaction {
        val entity = mapToEntity(user)
        Users.insert(entity)[Users.id]
    }
    
    override fun update(user: User): Boolean = transaction {
        val entity = mapToEntity(user)
        val updated = Users.updateWhere(entity) { Users.id eq entity.id }
        updated > 0
    }
    
    override fun delete(id: Int): Boolean = transaction {
        val deleted = Users.deleteWhere { Users.id eq id }
        deleted > 0
    }
    
    private fun mapToDomain(entity: UserEntity): User = User(
        id = entity.id,
        name = entity.name,
        email = entity.email,
        isActive = entity.status == "ACTIVE"
    )
    
    private fun mapToEntity(domain: User): UserEntity = UserEntity(
        id = domain.id,
        name = domain.name,
        email = domain.email,
        status = if (domain.isActive) "ACTIVE" else "INACTIVE"
    )
}
```

## Conclusion

Snitch's Exposed integration provides a powerful, flexible foundation for database operations in your applications. The combination of type safety, automatic mapping, and seamless transaction management creates a developer-friendly experience while maintaining the performance and flexibility needed for production applications.

By understanding the internal workings of this integration, you can leverage its full capabilities and customize it to fit your specific requirements.

## References

- [Exposed GitHub Repository](https://github.com/JetBrains/Exposed)
- [Exposed Wiki](https://github.com/JetBrains/Exposed/wiki)
- [H2 Database Documentation](https://h2database.com/html/main.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [HikariCP Connection Pool](https://github.com/brettwooldridge/HikariCP)