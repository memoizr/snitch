package me.snitchon.example

import me.snitchon.example.repository.dbSchema
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DB(private val database: Database) {

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