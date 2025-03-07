# Database Integration Tutorial

This tutorial walks you through setting up a simple Snitch application with database integration using the Exposed modules. You'll learn how to connect to a database, define tables, implement CRUD operations, and handle transactions.

## Prerequisites

- Basic knowledge of Kotlin and Snitch
- Gradle or Maven build system
- JDK 11 or higher

## Setup

Add the required dependencies to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-core:$snitchVersion")
    implementation("io.github.memoizr:snitch-exposed:$snitchVersion")
    
    // Choose one (or both) of these database modules
    implementation("io.github.memoizr:snitch-exposed-h2:$snitchVersion")      // For development/testing
    implementation("io.github.memoizr:snitch-exposed-postgres:$snitchVersion") // For production
}
```

## Step 1: Configure Database Connection

Let's start by setting up a database connection. We'll use H2 for development:

```kotlin
import snitch.exposed.h2.h2ConnectionConfig
import snitch.exposed.ExposedModule
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SchemaUtils

// Create an in-memory H2 database
val config = h2ConnectionConfig()
ExposedModule.connection(config)

// Initialize schema
transaction {
    // We'll define tables in the next step
}
```

For a production PostgreSQL setup, you would use:

```kotlin
import snitch.exposed.postgres.postgresConnectionConfig

val prodConfig = postgresConnectionConfig(
    host = "localhost",
    database = "myapp",
    user = "dbuser",
    password = "dbpassword"
)
ExposedModule.connection(prodConfig)
```

## Step 2: Define Domain Models and Tables

Now, let's define our domain models and database tables:

```kotlin
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

// Domain models
data class Task(
    val title: String,
    val description: String,
    val completed: Boolean = false,
    val dueDate: LocalDateTime? = null
)

data class TaskWithId(
    val id: Int,
    val title: String,
    val description: String,
    val completed: Boolean = false,
    val dueDate: LocalDateTime? = null
)

// Database table
object Tasks : Table("tasks") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val description = text("description")
    val completed = bool("completed").default(false)
    val dueDate = datetime("due_date").nullable()
    
    override val primaryKey = PrimaryKey(id)
}

// Initialize the schema
transaction {
    SchemaUtils.create(Tasks)
}
```

## Step 3: Implement CRUD Operations

Let's implement basic CRUD operations for our tasks:

```kotlin
import snitch.exposed.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class TaskRepository {
    // Create a new task
    fun createTask(task: Task): Int = transaction {
        Tasks.insert(task)[Tasks.id]
    }
    
    // Get all tasks
    fun getAllTasks(): List<TaskWithId> = transaction {
        Tasks.findAll<TaskWithId>()
    }
    
    // Get a task by ID
    fun getTaskById(id: Int): TaskWithId? = transaction {
        Tasks.findOneOrNull<TaskWithId> { Tasks.id eq id }
    }
    
    // Update a task
    fun updateTask(task: TaskWithId): Boolean = transaction {
        val updated = Tasks.updateWhere(task) { Tasks.id eq task.id }
        updated > 0
    }
    
    // Delete a task
    fun deleteTask(id: Int): Boolean = transaction {
        val deleted = Tasks.deleteWhere { Tasks.id eq id }
        deleted > 0
    }
    
    // Get completed tasks
    fun getCompletedTasks(): List<TaskWithId> = transaction {
        Tasks.findAll<TaskWithId> { Tasks.completed eq true }
    }
}
```

## Step 4: Integrate with Snitch Routes

Now let's create endpoints to interact with our tasks:

```kotlin
import snitch.router.routes
import snitch.parameters.path
import snitch.validation.ofInt
import snitch.exposed.withTransaction

val taskRepository = TaskRepository()
val id by path(ofInt)

val taskRoutes = routes {
    // Wrap all routes in a transaction
    withTransaction {
        // Get all tasks
        GET("tasks") isHandledBy {
            taskRepository.getAllTasks().ok
        }
        
        // Get a specific task
        GET("tasks" / id) isHandledBy {
            val taskId = request[id]
            val task = taskRepository.getTaskById(taskId)
            task?.ok ?: "Task not found".notFound()
        }
        
        // Create a new task
        POST("tasks") with body<Task>() isHandledBy {
            val task = body
            val newTaskId = taskRepository.createTask(task)
            val createdTask = taskRepository.getTaskById(newTaskId)
            createdTask!!.created
        }
        
        // Update a task
        PUT("tasks" / id) with body<Task>() isHandledBy {
            val taskId = request[id]
            val updatedTask = TaskWithId(
                id = taskId,
                title = body.title,
                description = body.description,
                completed = body.completed,
                dueDate = body.dueDate
            )
            
            val success = taskRepository.updateTask(updatedTask)
            if (success) {
                val task = taskRepository.getTaskById(taskId)
                task!!.ok
            } else {
                "Task not found".notFound()
            }
        }
        
        // Delete a task
        DELETE("tasks" / id) isHandledBy {
            val taskId = request[id]
            val deleted = taskRepository.deleteTask(taskId)
            if (deleted) {
                "Task deleted".ok
            } else {
                "Task not found".notFound()
            }
        }
        
        // Get completed tasks
        GET("tasks/completed") isHandledBy {
            taskRepository.getCompletedTasks().ok
        }
    }
}
```

## Step 5: Start the Application

Finally, let's set up the main application class:

```kotlin
import snitch.undertow.UndertowSnitchService
import snitch.parsers.GsonJsonParser
import snitch.config.SnitchConfig

fun main() {
    // Set up the database
    val config = h2ConnectionConfig()
    ExposedModule.connection(config)
    
    // Initialize schema
    transaction {
        SchemaUtils.create(Tasks)
    }
    
    // Create and start the server
    val service = UndertowSnitchService(
        GsonJsonParser,
        SnitchConfig(
            SnitchConfig.Service(
                port = 8080
            )
        )
    )
    
    service.onRoutes(taskRoutes).start()
    println("Server started on http://localhost:8080")
}
```

## Step 6: Test Your Application

You can now run your application and test the endpoints:

1. Create a task:
```bash
curl -X POST http://localhost:8080/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Learn Snitch Exposed","description":"Complete the database tutorial","completed":false}'
```

2. Get all tasks:
```bash
curl http://localhost:8080/tasks
```

3. Get a specific task:
```bash
curl http://localhost:8080/tasks/1
```

4. Update a task:
```bash
curl -X PUT http://localhost:8080/tasks/1 \
  -H "Content-Type: application/json" \
  -d '{"title":"Learn Snitch Exposed","description":"Completed the database tutorial","completed":true}'
```

5. Delete a task:
```bash
curl -X DELETE http://localhost:8080/tasks/1
```

## Advanced Features

Let's explore some advanced features of Snitch Exposed.

### Working with Value Classes

```kotlin
// Define a value class for stronger typing
@JvmInline
value class TaskId(val value: Int)

// Domain model using the value class
data class TaskWithValueId(
    val id: TaskId,
    val title: String,
    val description: String,
    val completed: Boolean = false,
    val dueDate: LocalDateTime? = null
)

// Using the value class in our repository
fun getTaskById(id: Int): TaskWithValueId? = transaction {
    Tasks.findOneOrNull<TaskWithValueId> { Tasks.id eq id }
}

// The mapping between Int and TaskId happens automatically
```

### Custom Type Mapping

```kotlin
// Let's say we want to store task status as a string in the database
// but use an enum in our domain model
enum class TaskStatus { TODO, IN_PROGRESS, DONE }

// Define custom mapping
AutoMapper.customMapping<String, TaskStatus>(
    from = { str -> TaskStatus.valueOf(str) },
    to = { status -> status.name }
)

// Now we can use TaskStatus directly in our domain model
data class TaskWithStatus(
    val id: Int,
    val title: String,
    val description: String,
    val status: TaskStatus
)

// And have it automatically mapped to/from String in the database
object TasksWithStatus : Table("tasks_with_status") {
    val id = integer("id").autoIncrement()
    val title = varchar("title", 255)
    val description = text("description")
    val status = varchar("status", 20)
    
    override val primaryKey = PrimaryKey(id)
}
```

### Batch Operations

For better performance when working with multiple entities:

```kotlin
fun createTasks(tasks: List<Task>): List<Int> = transaction {
    Tasks.batchInsert(tasks) { task ->
        this[Tasks.title] = task.title
        this[Tasks.description] = task.description
        this[Tasks.completed] = task.completed
        this[Tasks.dueDate] = task.dueDate
    }.map { it[Tasks.id] }
}
```

### Working with Relationships

Let's add a related entity to our tasks:

```kotlin
// Domain models
data class Tag(val name: String)
data class TagWithId(val id: Int, val name: String)
data class TaskWithTags(
    val id: Int,
    val title: String,
    val description: String,
    val completed: Boolean,
    val dueDate: LocalDateTime?,
    val tags: List<TagWithId>
)

// Database tables
object Tags : Table("tags") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100).uniqueIndex()
    
    override val primaryKey = PrimaryKey(id)
}

object TaskTags : Table("task_tags") {
    val taskId = integer("task_id") references Tasks.id
    val tagId = integer("tag_id") references Tags.id
    
    override val primaryKey = PrimaryKey(taskId, tagId)
}

// Repository methods for handling relationships
fun getTaskWithTags(taskId: Int): TaskWithTags? = transaction {
    val task = Tasks.findOneOrNull<TaskWithId> { Tasks.id eq taskId } ?: return@transaction null
    
    val tags = (TaskTags innerJoin Tags)
        .select { TaskTags.taskId eq taskId }
        .map { TagWithId(it[Tags.id], it[Tags.name]) }
    
    TaskWithTags(
        id = task.id,
        title = task.title,
        description = task.description,
        completed = task.completed,
        dueDate = task.dueDate,
        tags = tags
    )
}

fun addTagToTask(taskId: Int, tagId: Int) = transaction {
    TaskTags.insert {
        it[TaskTags.taskId] = taskId
        it[TaskTags.tagId] = tagId
    }
}
```

## Best Practices

1. **Separate concerns**: Place database-related code in repositories, keeping your routes clean.

2. **Use transactions appropriately**: Wrap related operations in transactions for data consistency.

3. **Error handling**: Implement proper exception handling:

```kotlin
withTransaction {
    POST("tasks") with body<Task>() isHandledBy {
        try {
            val task = body
            val id = taskRepository.createTask(task)
            TaskResponse(id, task.title).created
        } catch (e: Exception) {
            logger.error("Error creating task", e)
            "Failed to create task: ${e.message}".serverError()
        }
    }
}
```

4. **Testing**: Use H2 in-memory databases for testing:

```kotlin
@BeforeEach
fun setup() {
    // Create a fresh in-memory database for each test
    val config = h2ConnectionConfig(name = "test_${UUID.randomUUID()}")
    ExposedModule.connection(config)
    
    transaction {
        SchemaUtils.create(Tasks, Tags, TaskTags)
    }
}
```

5. **Managing connections**:
   - For development and testing, H2 in-memory is convenient
   - For production, use PostgreSQL with connection pooling
   - Always handle database connection lifecycle properly

## Conclusion

You now have a fully functional Snitch application with database integration. The Exposed modules provide a type-safe way to work with databases while eliminating much of the boilerplate typically required for database operations.

For more advanced uses, refer to:
- [Database Integration Guide](../resources/Database-Integration.md)
- [Exposed documentation](https://github.com/JetBrains/Exposed/wiki)
- [Example projects in the Snitch repository](https://github.com/memoizr/snitch/tree/master/example)