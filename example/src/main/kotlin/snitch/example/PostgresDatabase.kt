package snitch.example

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import snitch.example.database.dbSchema

class PostgresDatabase(private val database: Database) {

    fun createSchema() {
        transaction(database) {
            SchemaUtils.create(tables=*dbSchema, true)
        }
    }

    fun addMissingColumns() {
        printTime {
            transaction(database) {
                printTime {
                    SchemaUtils.addMissingColumnsStatements(*dbSchema)
                }
            }
        }
    }

    fun dropSchema() {
        transaction(database) {
            SchemaUtils.drop(tables=*dbSchema,true)
        }
    }
}