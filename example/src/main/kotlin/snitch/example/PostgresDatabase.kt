package snitch.example

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import snitch.example.database.dbSchema

class PostgresDatabase(private val database: Database) {

    fun createSchema() {
        transaction(database) {
            SchemaUtils.create(*dbSchema)
        }
    }

    fun addMissingColumns() {
        transaction(database) {
            SchemaUtils.addMissingColumnsStatements(*dbSchema)
        }
    }

    fun dropSchema() {
        transaction(database) {
            SchemaUtils.drop(*dbSchema)
        }
    }
}