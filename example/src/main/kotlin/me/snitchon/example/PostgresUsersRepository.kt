package me.snitchon.example

import me.snitchon.example.repository.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.Exception
import java.util.*

class PostgresUsersRepository(private val db: Database) : UsersRepository {

    override fun putUser(user: CreateUserAction) = transaction(db = db) {
        try {
            Users.insert {
                it[id] = UUID.randomUUID().toString()
                it[name] = user.name
                it[email] = user.email.value
                it[hash] = user.password.hash
            }
            TransactionResult.Success()
        } catch (e: Exception) {
            TransactionResult.Failure()
        }
    }

    override fun findHashBy(email: Email): Hash? = transaction {
        Users
            .select { Users.email eq email.value }
            .map { Hash(it[Users.hash]) }
            .singleOrNull()
    }
}