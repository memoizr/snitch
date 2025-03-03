"use strict";(self.webpackChunkguides=self.webpackChunkguides||[]).push([[182],{3517:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>r,contentTitle:()=>l,default:()=>p,frontMatter:()=>o,metadata:()=>a,toc:()=>d});const a=JSON.parse('{"id":"tutorials/DatabaseIntegration","title":"Database Integration Tutorial","description":"This tutorial walks you through setting up a simple Snitch application with database integration using the Exposed modules. You\'ll learn how to connect to a database, define tables, implement CRUD operations, and handle transactions.","source":"@site/docs/tutorials/DatabaseIntegration.md","sourceDirName":"tutorials","slug":"/tutorials/DatabaseIntegration","permalink":"/docs/tutorials/DatabaseIntegration","draft":false,"unlisted":false,"editUrl":"https://github.com/memoizr/snitch/tree/master/guides/docs/tutorials/DatabaseIntegration.md","tags":[],"version":"current","frontMatter":{},"sidebar":"tutorialSidebar","previous":{"title":"Snitch Tutorials","permalink":"/docs/tutorials/"},"next":{"title":"Handling Parameters in Snitch","permalink":"/docs/tutorials/HandlingParameters"}}');var s=t(4848),i=t(8453);const o={},l="Database Integration Tutorial",r={},d=[{value:"Prerequisites",id:"prerequisites",level:2},{value:"Setup",id:"setup",level:2},{value:"Step 1: Configure Database Connection",id:"step-1-configure-database-connection",level:2},{value:"Step 2: Define Domain Models and Tables",id:"step-2-define-domain-models-and-tables",level:2},{value:"Step 3: Implement CRUD Operations",id:"step-3-implement-crud-operations",level:2},{value:"Step 4: Integrate with Snitch Routes",id:"step-4-integrate-with-snitch-routes",level:2},{value:"Step 5: Start the Application",id:"step-5-start-the-application",level:2},{value:"Step 6: Test Your Application",id:"step-6-test-your-application",level:2},{value:"Advanced Features",id:"advanced-features",level:2},{value:"Working with Value Classes",id:"working-with-value-classes",level:3},{value:"Custom Type Mapping",id:"custom-type-mapping",level:3},{value:"Batch Operations",id:"batch-operations",level:3},{value:"Working with Relationships",id:"working-with-relationships",level:3},{value:"Best Practices",id:"best-practices",level:2},{value:"Conclusion",id:"conclusion",level:2}];function c(e){const n={a:"a",code:"code",h1:"h1",h2:"h2",h3:"h3",header:"header",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,i.R)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsx)(n.header,{children:(0,s.jsx)(n.h1,{id:"database-integration-tutorial",children:"Database Integration Tutorial"})}),"\n",(0,s.jsx)(n.p,{children:"This tutorial walks you through setting up a simple Snitch application with database integration using the Exposed modules. You'll learn how to connect to a database, define tables, implement CRUD operations, and handle transactions."}),"\n",(0,s.jsx)(n.h2,{id:"prerequisites",children:"Prerequisites"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsx)(n.li,{children:"Basic knowledge of Kotlin and Snitch"}),"\n",(0,s.jsx)(n.li,{children:"Gradle or Maven build system"}),"\n",(0,s.jsx)(n.li,{children:"JDK 11 or higher"}),"\n"]}),"\n",(0,s.jsx)(n.h2,{id:"setup",children:"Setup"}),"\n",(0,s.jsxs)(n.p,{children:["Add the required dependencies to your ",(0,s.jsx)(n.code,{children:"build.gradle.kts"}),":"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'dependencies {\n    implementation("io.github.memoizr:snitch-core:$snitchVersion")\n    implementation("io.github.memoizr:snitch-exposed:$snitchVersion")\n    \n    // Choose one (or both) of these database modules\n    implementation("io.github.memoizr:snitch-exposed-h2:$snitchVersion")      // For development/testing\n    implementation("io.github.memoizr:snitch-exposed-postgres:$snitchVersion") // For production\n}\n'})}),"\n",(0,s.jsx)(n.h2,{id:"step-1-configure-database-connection",children:"Step 1: Configure Database Connection"}),"\n",(0,s.jsx)(n.p,{children:"Let's start by setting up a database connection. We'll use H2 for development:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:"import snitch.exposed.h2.h2ConnectionConfig\nimport snitch.exposed.ExposedModule\nimport org.jetbrains.exposed.sql.transactions.transaction\nimport org.jetbrains.exposed.sql.SchemaUtils\n\n// Create an in-memory H2 database\nval config = h2ConnectionConfig()\nExposedModule.connection(config)\n\n// Initialize schema\ntransaction {\n    // We'll define tables in the next step\n}\n"})}),"\n",(0,s.jsx)(n.p,{children:"For a production PostgreSQL setup, you would use:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'import snitch.exposed.postgres.postgresConnectionConfig\n\nval prodConfig = postgresConnectionConfig(\n    host = "localhost",\n    database = "myapp",\n    user = "dbuser",\n    password = "dbpassword"\n)\nExposedModule.connection(prodConfig)\n'})}),"\n",(0,s.jsx)(n.h2,{id:"step-2-define-domain-models-and-tables",children:"Step 2: Define Domain Models and Tables"}),"\n",(0,s.jsx)(n.p,{children:"Now, let's define our domain models and database tables:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'import org.jetbrains.exposed.sql.Table\nimport org.jetbrains.exposed.sql.javatime.datetime\nimport java.time.LocalDateTime\n\n// Domain models\ndata class Task(\n    val title: String,\n    val description: String,\n    val completed: Boolean = false,\n    val dueDate: LocalDateTime? = null\n)\n\ndata class TaskWithId(\n    val id: Int,\n    val title: String,\n    val description: String,\n    val completed: Boolean = false,\n    val dueDate: LocalDateTime? = null\n)\n\n// Database table\nobject Tasks : Table("tasks") {\n    val id = integer("id").autoIncrement()\n    val title = varchar("title", 255)\n    val description = text("description")\n    val completed = bool("completed").default(false)\n    val dueDate = datetime("due_date").nullable()\n    \n    override val primaryKey = PrimaryKey(id)\n}\n\n// Initialize the schema\ntransaction {\n    SchemaUtils.create(Tasks)\n}\n'})}),"\n",(0,s.jsx)(n.h2,{id:"step-3-implement-crud-operations",children:"Step 3: Implement CRUD Operations"}),"\n",(0,s.jsx)(n.p,{children:"Let's implement basic CRUD operations for our tasks:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:"import snitch.exposed.*\nimport org.jetbrains.exposed.sql.transactions.transaction\nimport org.jetbrains.exposed.sql.SqlExpressionBuilder.eq\n\nclass TaskRepository {\n    // Create a new task\n    fun createTask(task: Task): Int = transaction {\n        Tasks.insert(task)[Tasks.id]\n    }\n    \n    // Get all tasks\n    fun getAllTasks(): List<TaskWithId> = transaction {\n        Tasks.findAll<TaskWithId>()\n    }\n    \n    // Get a task by ID\n    fun getTaskById(id: Int): TaskWithId? = transaction {\n        Tasks.findOneOrNull<TaskWithId> { Tasks.id eq id }\n    }\n    \n    // Update a task\n    fun updateTask(task: TaskWithId): Boolean = transaction {\n        val updated = Tasks.updateWhere(task) { Tasks.id eq task.id }\n        updated > 0\n    }\n    \n    // Delete a task\n    fun deleteTask(id: Int): Boolean = transaction {\n        val deleted = Tasks.deleteWhere { Tasks.id eq id }\n        deleted > 0\n    }\n    \n    // Get completed tasks\n    fun getCompletedTasks(): List<TaskWithId> = transaction {\n        Tasks.findAll<TaskWithId> { Tasks.completed eq true }\n    }\n}\n"})}),"\n",(0,s.jsx)(n.h2,{id:"step-4-integrate-with-snitch-routes",children:"Step 4: Integrate with Snitch Routes"}),"\n",(0,s.jsx)(n.p,{children:"Now let's create endpoints to interact with our tasks:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'import snitch.router.routes\nimport snitch.parameters.path\nimport snitch.validation.ofInt\nimport snitch.exposed.withTransaction\n\nval taskRepository = TaskRepository()\nval id by path(ofInt)\n\nval taskRoutes = routes {\n    // Wrap all routes in a transaction\n    withTransaction {\n        // Get all tasks\n        GET("tasks") isHandledBy {\n            taskRepository.getAllTasks().ok\n        }\n        \n        // Get a specific task\n        GET("tasks" / id) isHandledBy {\n            val taskId = request[id]\n            val task = taskRepository.getTaskById(taskId)\n            task?.ok ?: "Task not found".notFound()\n        }\n        \n        // Create a new task\n        POST("tasks") with body<Task>() isHandledBy {\n            val task = body\n            val newTaskId = taskRepository.createTask(task)\n            val createdTask = taskRepository.getTaskById(newTaskId)\n            createdTask!!.created\n        }\n        \n        // Update a task\n        PUT("tasks" / id) with body<Task>() isHandledBy {\n            val taskId = request[id]\n            val updatedTask = TaskWithId(\n                id = taskId,\n                title = body.title,\n                description = body.description,\n                completed = body.completed,\n                dueDate = body.dueDate\n            )\n            \n            val success = taskRepository.updateTask(updatedTask)\n            if (success) {\n                val task = taskRepository.getTaskById(taskId)\n                task!!.ok\n            } else {\n                "Task not found".notFound()\n            }\n        }\n        \n        // Delete a task\n        DELETE("tasks" / id) isHandledBy {\n            val taskId = request[id]\n            val deleted = taskRepository.deleteTask(taskId)\n            if (deleted) {\n                "Task deleted".ok\n            } else {\n                "Task not found".notFound()\n            }\n        }\n        \n        // Get completed tasks\n        GET("tasks/completed") isHandledBy {\n            taskRepository.getCompletedTasks().ok\n        }\n    }\n}\n'})}),"\n",(0,s.jsx)(n.h2,{id:"step-5-start-the-application",children:"Step 5: Start the Application"}),"\n",(0,s.jsx)(n.p,{children:"Finally, let's set up the main application class:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'import snitch.undertow.UndertowSnitchService\nimport snitch.parsers.GsonJsonParser\nimport snitch.config.SnitchConfig\n\nfun main() {\n    // Set up the database\n    val config = h2ConnectionConfig()\n    ExposedModule.connection(config)\n    \n    // Initialize schema\n    transaction {\n        SchemaUtils.create(Tasks)\n    }\n    \n    // Create and start the server\n    val service = UndertowSnitchService(\n        GsonJsonParser,\n        SnitchConfig(\n            SnitchConfig.Service(\n                port = 8080\n            )\n        )\n    )\n    \n    service.onRoutes(taskRoutes).start()\n    println("Server started on http://localhost:8080")\n}\n'})}),"\n",(0,s.jsx)(n.h2,{id:"step-6-test-your-application",children:"Step 6: Test Your Application"}),"\n",(0,s.jsx)(n.p,{children:"You can now run your application and test the endpoints:"}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsx)(n.li,{children:"Create a task:"}),"\n"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-bash",children:'curl -X POST http://localhost:8080/tasks \\\n  -H "Content-Type: application/json" \\\n  -d \'{"title":"Learn Snitch Exposed","description":"Complete the database tutorial","completed":false}\'\n'})}),"\n",(0,s.jsxs)(n.ol,{start:"2",children:["\n",(0,s.jsx)(n.li,{children:"Get all tasks:"}),"\n"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-bash",children:"curl http://localhost:8080/tasks\n"})}),"\n",(0,s.jsxs)(n.ol,{start:"3",children:["\n",(0,s.jsx)(n.li,{children:"Get a specific task:"}),"\n"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-bash",children:"curl http://localhost:8080/tasks/1\n"})}),"\n",(0,s.jsxs)(n.ol,{start:"4",children:["\n",(0,s.jsx)(n.li,{children:"Update a task:"}),"\n"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-bash",children:'curl -X PUT http://localhost:8080/tasks/1 \\\n  -H "Content-Type: application/json" \\\n  -d \'{"title":"Learn Snitch Exposed","description":"Completed the database tutorial","completed":true}\'\n'})}),"\n",(0,s.jsxs)(n.ol,{start:"5",children:["\n",(0,s.jsx)(n.li,{children:"Delete a task:"}),"\n"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-bash",children:"curl -X DELETE http://localhost:8080/tasks/1\n"})}),"\n",(0,s.jsx)(n.h2,{id:"advanced-features",children:"Advanced Features"}),"\n",(0,s.jsx)(n.p,{children:"Let's explore some advanced features of Snitch Exposed."}),"\n",(0,s.jsx)(n.h3,{id:"working-with-value-classes",children:"Working with Value Classes"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:"// Define a value class for stronger typing\n@JvmInline\nvalue class TaskId(val value: Int)\n\n// Domain model using the value class\ndata class TaskWithValueId(\n    val id: TaskId,\n    val title: String,\n    val description: String,\n    val completed: Boolean = false,\n    val dueDate: LocalDateTime? = null\n)\n\n// Using the value class in our repository\nfun getTaskById(id: Int): TaskWithValueId? = transaction {\n    Tasks.findOneOrNull<TaskWithValueId> { Tasks.id eq id }\n}\n\n// The mapping between Int and TaskId happens automatically\n"})}),"\n",(0,s.jsx)(n.h3,{id:"custom-type-mapping",children:"Custom Type Mapping"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'// Let\'s say we want to store task status as a string in the database\n// but use an enum in our domain model\nenum class TaskStatus { TODO, IN_PROGRESS, DONE }\n\n// Define custom mapping\nAutoMapper.customMapping<String, TaskStatus>(\n    from = { str -> TaskStatus.valueOf(str) },\n    to = { status -> status.name }\n)\n\n// Now we can use TaskStatus directly in our domain model\ndata class TaskWithStatus(\n    val id: Int,\n    val title: String,\n    val description: String,\n    val status: TaskStatus\n)\n\n// And have it automatically mapped to/from String in the database\nobject TasksWithStatus : Table("tasks_with_status") {\n    val id = integer("id").autoIncrement()\n    val title = varchar("title", 255)\n    val description = text("description")\n    val status = varchar("status", 20)\n    \n    override val primaryKey = PrimaryKey(id)\n}\n'})}),"\n",(0,s.jsx)(n.h3,{id:"batch-operations",children:"Batch Operations"}),"\n",(0,s.jsx)(n.p,{children:"For better performance when working with multiple entities:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:"fun createTasks(tasks: List<Task>): List<Int> = transaction {\n    Tasks.batchInsert(tasks) { task ->\n        this[Tasks.title] = task.title\n        this[Tasks.description] = task.description\n        this[Tasks.completed] = task.completed\n        this[Tasks.dueDate] = task.dueDate\n    }.map { it[Tasks.id] }\n}\n"})}),"\n",(0,s.jsx)(n.h3,{id:"working-with-relationships",children:"Working with Relationships"}),"\n",(0,s.jsx)(n.p,{children:"Let's add a related entity to our tasks:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'// Domain models\ndata class Tag(val name: String)\ndata class TagWithId(val id: Int, val name: String)\ndata class TaskWithTags(\n    val id: Int,\n    val title: String,\n    val description: String,\n    val completed: Boolean,\n    val dueDate: LocalDateTime?,\n    val tags: List<TagWithId>\n)\n\n// Database tables\nobject Tags : Table("tags") {\n    val id = integer("id").autoIncrement()\n    val name = varchar("name", 100).uniqueIndex()\n    \n    override val primaryKey = PrimaryKey(id)\n}\n\nobject TaskTags : Table("task_tags") {\n    val taskId = integer("task_id") references Tasks.id\n    val tagId = integer("tag_id") references Tags.id\n    \n    override val primaryKey = PrimaryKey(taskId, tagId)\n}\n\n// Repository methods for handling relationships\nfun getTaskWithTags(taskId: Int): TaskWithTags? = transaction {\n    val task = Tasks.findOneOrNull<TaskWithId> { Tasks.id eq taskId } ?: return@transaction null\n    \n    val tags = (TaskTags innerJoin Tags)\n        .select { TaskTags.taskId eq taskId }\n        .map { TagWithId(it[Tags.id], it[Tags.name]) }\n    \n    TaskWithTags(\n        id = task.id,\n        title = task.title,\n        description = task.description,\n        completed = task.completed,\n        dueDate = task.dueDate,\n        tags = tags\n    )\n}\n\nfun addTagToTask(taskId: Int, tagId: Int) = transaction {\n    TaskTags.insert {\n        it[TaskTags.taskId] = taskId\n        it[TaskTags.tagId] = tagId\n    }\n}\n'})}),"\n",(0,s.jsx)(n.h2,{id:"best-practices",children:"Best Practices"}),"\n",(0,s.jsxs)(n.ol,{children:["\n",(0,s.jsxs)(n.li,{children:["\n",(0,s.jsxs)(n.p,{children:[(0,s.jsx)(n.strong,{children:"Separate concerns"}),": Place database-related code in repositories, keeping your routes clean."]}),"\n"]}),"\n",(0,s.jsxs)(n.li,{children:["\n",(0,s.jsxs)(n.p,{children:[(0,s.jsx)(n.strong,{children:"Use transactions appropriately"}),": Wrap related operations in transactions for data consistency."]}),"\n"]}),"\n",(0,s.jsxs)(n.li,{children:["\n",(0,s.jsxs)(n.p,{children:[(0,s.jsx)(n.strong,{children:"Error handling"}),": Implement proper exception handling:"]}),"\n"]}),"\n"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'withTransaction {\n    POST("tasks") with body<Task>() isHandledBy {\n        try {\n            val task = body\n            val id = taskRepository.createTask(task)\n            TaskResponse(id, task.title).created\n        } catch (e: Exception) {\n            logger.error("Error creating task", e)\n            "Failed to create task: ${e.message}".serverError()\n        }\n    }\n}\n'})}),"\n",(0,s.jsxs)(n.ol,{start:"4",children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"Testing"}),": Use H2 in-memory databases for testing:"]}),"\n"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'@BeforeEach\nfun setup() {\n    // Create a fresh in-memory database for each test\n    val config = h2ConnectionConfig(name = "test_${UUID.randomUUID()}")\n    ExposedModule.connection(config)\n    \n    transaction {\n        SchemaUtils.create(Tasks, Tags, TaskTags)\n    }\n}\n'})}),"\n",(0,s.jsxs)(n.ol,{start:"5",children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"Managing connections"}),":","\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsx)(n.li,{children:"For development and testing, H2 in-memory is convenient"}),"\n",(0,s.jsx)(n.li,{children:"For production, use PostgreSQL with connection pooling"}),"\n",(0,s.jsx)(n.li,{children:"Always handle database connection lifecycle properly"}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,s.jsx)(n.h2,{id:"conclusion",children:"Conclusion"}),"\n",(0,s.jsx)(n.p,{children:"You now have a fully functional Snitch application with database integration. The Exposed modules provide a type-safe way to work with databases while eliminating much of the boilerplate typically required for database operations."}),"\n",(0,s.jsx)(n.p,{children:"For more advanced uses, refer to:"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsx)(n.li,{children:(0,s.jsx)(n.a,{href:"/docs/resources/Database-Integration",children:"Database Integration Guide"})}),"\n",(0,s.jsx)(n.li,{children:(0,s.jsx)(n.a,{href:"https://github.com/JetBrains/Exposed/wiki",children:"Exposed documentation"})}),"\n",(0,s.jsx)(n.li,{children:(0,s.jsx)(n.a,{href:"https://github.com/memoizr/snitch/tree/master/example",children:"Example projects in the Snitch repository"})}),"\n"]})]})}function p(e={}){const{wrapper:n}={...(0,i.R)(),...e.components};return n?(0,s.jsx)(n,{...e,children:(0,s.jsx)(c,{...e})}):c(e)}},8453:(e,n,t)=>{t.d(n,{R:()=>o,x:()=>l});var a=t(6540);const s={},i=a.createContext(s);function o(e){const n=a.useContext(i);return a.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function l(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:o(e.components),a.createElement(i.Provider,{value:n},e.children)}}}]);