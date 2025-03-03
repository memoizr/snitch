package snitch.exposed

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction

class ExposedDatabase(private val database: Database, private vararg val schema: Table) {

    fun createSchema() {
        transaction(database) {
            SchemaUtils.create(tables = schema, true)
        }
    }

    fun addMissingColumns() {
        transaction(database) {
            SchemaUtils.addMissingColumnsStatements(*schema)
        }
    }

    fun dropSchema() {
        transaction(database) {
            SchemaUtils.drop(tables = schema, true)
        }
    }
}
