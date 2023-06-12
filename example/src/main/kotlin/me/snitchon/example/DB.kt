package me.snitchon.example

import me.snitchon.example.repository.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DB(private val database: Database) {
    private val schema = listOf(Users).toTypedArray()

    fun createSchema() {
        transaction(database) {
            SchemaUtils.create(*schema)
        }
    }

    fun addMissingColumns() {
        transaction(database) {
            SchemaUtils.addMissingColumnsStatements(*schema)
        }
    }

    fun dropSchema() {
        transaction(database) {
            SchemaUtils.drop(*schema)
        }
    }
}