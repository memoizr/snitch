package snitch.exposed

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManager
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

data class DatabaseConnectionConfig(
    val url: String,
    val driver: String,
    val user: String = "",
    val password: String = "",
    val setupConnection: (Connection) -> Unit = {},
    val databaseConfig: DatabaseConfig? = null,
    val manager: (Database) -> TransactionManager = { ThreadLocalTransactionManager(it) }
)