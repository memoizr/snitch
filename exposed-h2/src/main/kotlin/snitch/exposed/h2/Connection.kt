package snitch.exposed.h2

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManager
import org.jetbrains.exposed.sql.transactions.TransactionManager
import snitch.exposed.DatabaseConnectionConfig
import java.sql.Connection

fun h2ConnectionConfig(
    url: String = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;",
    driver: String=  "org.h2.Driver",
    user: String = "root",
    password: String = "",
    setupConnection: (Connection) -> Unit = {},
    databaseConfig: DatabaseConfig? = null,
    manager: (Database) -> TransactionManager = { ThreadLocalTransactionManager(it) }
) = DatabaseConnectionConfig(
    url = url,
    driver = driver,
    user = user,
    password = password,
    setupConnection = setupConnection,
    databaseConfig = databaseConfig,
    manager = manager,
)
