package snitch.exposed.postgres

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManager
import org.jetbrains.exposed.sql.transactions.TransactionManager
import snitch.exposed.DatabaseConnectionConfig
import java.sql.Connection

fun postgresConnectionConfig(
    url: String = "jdbc:postgresql://localhost:5432/postgres",
    driver: String= "org.postgresql.Driver",
    user: String = "postgres",
    password: String = "postgres",
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
