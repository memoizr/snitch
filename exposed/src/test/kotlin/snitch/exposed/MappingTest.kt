package snitch.exposed

import com.memoizr.assertk.expect
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotNull

class MappingTest {
    
    // Test table for mapping tests
    object TestUsers : Table("test_users") {
        val id = integer("id").autoIncrement()
        val name = varchar("name", 255)
        val email = varchar("email", 255)
        val age = integer("age")

        override val primaryKey = PrimaryKey(id)
    }

    // Domain models
    data class User(val name: String, val email: String, val age: Int)
    data class UserWithId(val id: Int = 0, val name: String, val email: String, val age: Int)
    
    // Value class for testing value class mapping
    @JvmInline
    value class UserId(val value: Int)
    
    data class UserWithValueId(val id: UserId, val name: String, val email: String, val age: Int)

    @BeforeEach
    fun setup() {
        // Connect to in-memory H2 database
        ExposedModule.connection(databaseTestConfig)
        // Create test tables
        transaction {
            SchemaUtils.create(TestUsers)
        }
    }

    @AfterEach
    fun tearDown() {
        transaction {
            SchemaUtils.drop(TestUsers)
        }
    }

    @Test
    fun `basic to() conversion between similar objects`() {
        // Given
        val user = User("John Doe", "john@example.com", 30)
        
        // When
        val userWithId = user.to<UserWithId>()
        
        // Then
        expect that userWithId.name isEqualTo "John Doe"
        expect that userWithId.email isEqualTo "john@example.com"
        expect that userWithId.age isEqualTo 30
        expect that userWithId.id isEqualTo 0 // Default value for Int when not provided
    }

    @JvmInline
    value class StringWrapper(val wrapped: String)

    @Test
    @Disabled
    fun `conversion with custom mapping`() {
        // Given

        AutoMapper.customMapping<User, StringWrapper>(
            from = { user -> StringWrapper("${user.name}:${user.email}:${user.age}") },
            to = { str ->
                val parts = str.wrapped.split(":")
                User(parts[0], parts[1], parts[2].toInt()) 
            }
        )
        
        val user = User("John Doe", "john@example.com", 30)
        
        // When
        val str = user.to<StringWrapper>()
        val backToUser = str.to<User>()
        
        // Then
        expect that str isEqualTo StringWrapper("John Doe:john@example.com:30")
        expect that backToUser.name isEqualTo "John Doe"
        expect that backToUser.email isEqualTo "john@example.com"
        expect that backToUser.age isEqualTo 30
    }

    @Test
    fun `conversion with value classes`() {
        // Given
        val userId = UserId(42)
        val user = UserWithValueId(userId, "John Doe", "john@example.com", 30)

        // When
        val userWithValueId = user.to<UserWithValueId>()
        val unwrappedId = userId.unwrap()
        
        // Then
        expect that userWithValueId.name isEqualTo "John Doe" 
        expect that userWithValueId.id.value isEqualTo 42 // Default value
        expect that unwrappedId isEqualTo 42
    }

    @Test
    fun `Table_to() should convert ResultRow to domain object`() {
        // Given a user in the database
        val userId = transaction {
            TestUsers.insert {
                it[name] = "Alice Smith"
                it[email] = "alice@example.com"
                it[age] = 25
            }[TestUsers.id]
        }
        
        // When
        val userResult = transaction {
            TestUsers.select { TestUsers.id eq userId }
                .map { TestUsers.to<UserWithId>(it) }
                .single()
        }
        
        // Then
        expect that userResult.id isEqualTo userId
        expect that userResult.name isEqualTo "Alice Smith"
        expect that userResult.email isEqualTo "alice@example.com"
        expect that userResult.age isEqualTo 25
    }

    @Test
    fun `insert() should auto-map domain object to table columns`() {
        // Given
        val user = User("Bob Johnson", "bob@example.com", 40)
        
        // When
        val insertResult = transaction {
            TestUsers.insert(user)
        }
        
        // Then
        val userId = insertResult[TestUsers.id]
        assertNotNull(userId)
        
        // Verify the data was inserted correctly
        val insertedUser = transaction {
            TestUsers.select { TestUsers.id eq userId }
                .map { 
                    UserWithId(
                        it[TestUsers.id],
                        it[TestUsers.name],
                        it[TestUsers.email],
                        it[TestUsers.age]
                    )
                }
                .single()
        }
        
        expect that insertedUser.name isEqualTo "Bob Johnson"
        expect that insertedUser.email isEqualTo "bob@example.com"
        expect that insertedUser.age isEqualTo 40
    }

    @Test
    fun `findAll() should return all mapped domain objects`() {
        // Given
        transaction {
            TestUsers.insert { 
                it[name] = "User 1"
                it[email] = "user1@example.com" 
                it[age] = 20
            }
            TestUsers.insert { 
                it[name] = "User 2"
                it[email] = "user2@example.com" 
                it[age] = 30
            }
        }
        
        // When
        val users = transaction {
            TestUsers.findAll<UserWithId>()
        }
        
        // Then
        expect that users.size isEqualTo 2
        expect that users.map { it.name }.containsAll(listOf("User 1", "User 2"))
    }

    @Test
    fun `findOne() should return a single mapped domain object`() {
        // Given
        transaction {
            TestUsers.insert { 
                it[name] = "Unique User"
                it[email] = "unique@example.com" 
                it[age] = 99
            }
        }
        
        // When
        val user = transaction {
            TestUsers.findOne<UserWithId> { TestUsers.name eq "Unique User" }
        }
        
        // Then
        expect that user.name isEqualTo "Unique User"
        expect that user.email isEqualTo "unique@example.com"
        expect that user.age isEqualTo 99
    }

    @Test
    fun `findOneOrNull() should return null when no match found`() {
        // When
        val user = transaction {
            TestUsers.findOneOrNull<UserWithId> { TestUsers.name eq "Non-existent User" }
        }
        
        // Then
        expect that user isEqualTo null
    }
    
    @Test
    fun `updateWhere() updates entity data based on domain object`() {
        // Given
        val userId = transaction {
            TestUsers.insert {
                it[name] = "Original Name"
                it[email] = "original@example.com"
                it[age] = 25
            }[TestUsers.id]
        }
        
        // When
        val updatedUser = UserWithId(userId, "Updated Name", "updated@example.com", 26)
        transaction {
            TestUsers.updateWhere(updatedUser) { TestUsers.id eq userId }
        }
        
        // Then
        val resultUser = transaction {
            TestUsers.findOne<UserWithId> { TestUsers.id eq userId }
        }
        
        expect that resultUser.name isEqualTo "Updated Name"
        expect that resultUser.email isEqualTo "updated@example.com"
        expect that resultUser.age isEqualTo 26
    }
    
    @Test
    @Disabled
    fun `error handling during mapping should provide useful error information`() {
        // Given
        data class InvalidUser(val invalidField: String)
        
        // When & Then
        val exception = assertThrows<Exception> {
            transaction {
                val resultRow = TestUsers.select { TestUsers.id eq 1 }.firstOrNull()
                if (resultRow != null) {
                    resultRow.to<InvalidUser>()
                }
            }
        }
        
        // Just verify an exception is thrown
        assertNotNull(exception)
    }
}