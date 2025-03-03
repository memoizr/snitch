package snitch.exposed

import com.memoizr.assertk.expect
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import snitch.exposed.ExposedModule.connection
import snitch.parameters.path
import snitch.parsers.GsonJsonParser.parse
import snitch.tests.InlineSnitchTest

val databaseTestConfig = DatabaseConnectionConfig(
    url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
    driver = "org.h2.Driver",
    user = "root",
    password = ""
)

// Define a test table to verify transaction behavior
object TestItems : Table("test_items") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 255)
    val value = integer("value")

    override val primaryKey = PrimaryKey(id)
}

val id by path(ofInt)

class ExposedTestInt : InlineSnitchTest({ _, port ->
    testRoutes("", this)(port)
        .handleException(RuntimeException::class) {
            "Transaction failed".serverError()
        }
}) {
    init {
        connection(databaseTestConfig)
    }

    data class Item(
        val name: String,
        val value: Int
    )

    data class ItemResponse(
        val id: Int,
        val name: String,
        val value: Int
    )

    @BeforeEach
    fun before() {
        transaction { SchemaUtils.create(TestItems) }
    }

    @AfterEach
    fun after() {
        transaction { SchemaUtils.drop(TestItems) }
    }

    @Test
    fun `verifies transaction is applied to endpoint handlers - successful transaction`() {
        given {
            // This endpoint is wrapped in a transaction and should commit changes
            withTransaction {
                POST("items") with body<Item>() isHandledBy {
                    val item = body
                    val id = TestItems.insert {
                        it[name] = item.name
                        it[value] = item.value
                    }[TestItems.id]

                    ItemResponse(id, item.name, item.value).ok
                }
            }

            // This endpoint is used to verify the database state
            GET("items" / id) isHandledBy {
                val itemId = request[id]
                val item = transaction {
                    TestItems.select { TestItems.id eq itemId }
                        .map {
                            ItemResponse(
                                it[TestItems.id],
                                it[TestItems.name],
                                it[TestItems.value]
                            )
                        }
                        .firstOrNull()
                }

                item?.ok ?: "Item not found".notFound()
            }
        } then {
            // Create an item using the transactional endpoint
            POST("/items") withBody (Item("test-item", 100)) expect {
                expect that it.statusCode() isEqualTo 200
                val response = it.body().parse(ItemResponse::class.java)
                expect that response.name isEqualTo "test-item"
                expect that response.value isEqualTo 100

                // Verify the item was actually saved in the database
                GET("/items/${response.id}") expect { getResponse ->
                    expect that getResponse.statusCode() isEqualTo 200
                    val getItem = getResponse.body().parse(ItemResponse::class.java)
                    expect that getItem.name isEqualTo "test-item"
                    expect that getItem.value isEqualTo 100
                }
            }
        }
    }

    @Test
    fun `verifies transaction is applied to endpoint handlers - failed transaction`() {
        given {
            // This endpoint is wrapped in a transaction but throws an exception,
            // so changes should be rolled back
            withTransaction {
                POST("items-with-error") with body<Item>() isHandledBy {
                    val item = body

                    // Insert the item
                    val id = TestItems.insert {
                        it[name] = item.name
                        it[value] = item.value
                    }[TestItems.id]

                    // Throw an exception to trigger transaction rollback
                    throw RuntimeException("Simulated error to trigger rollback")

                    // This line will never be reached
                    ItemResponse(id, item.name, item.value).ok
                }
            }


            // This endpoint counts all items to verify rollback
            GET("items-count") isHandledBy {
                val count = transaction {
                    TestItems.selectAll().count()
                }

                mapOf("count" to count).ok
            }
        } then {
            // Verify no items exist initially
            GET("/items-count") expect {
                expect that it.statusCode() isEqualTo 200
                val response = it.body()
                expect that response contains "\"count\":0"
            }

            // Try to create an item with the endpoint that will fail
            POST("/items-with-error") withBody (Item("should-rollback", 200)) expect {
                expect that it.statusCode() isEqualTo 500
                expect that it.body() contains "Transaction failed"
            }

            // Verify the item was NOT saved due to transaction rollback
            GET("/items-count") expect {
                expect that it.statusCode() isEqualTo 200
                val response = it.body()
                expect that response contains "\"count\":0"
            }
        }
    }
}
