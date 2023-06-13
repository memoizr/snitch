package me.snitchon.example.database.repositories.users

import me.snitchon.example.database.TransactionResult
import me.snitchon.example.database.Users
import me.snitchon.example.database.toErrorCode
import me.snitchon.example.types.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.sql.SQLException
import java.util.*

class PostgresUsersRepository : UsersRepository {

    override fun putUser(user: CreateUserAction) =
        try {
            Users.insert {
                it[id] = user.userId?.value ?: UUID.randomUUID().toString()
                it[name] = user.name.value
                it[email] = user.email.value
                it[hash] = user.hash.value
            }
            TransactionResult.Success
        } catch (e: SQLException) {
            TransactionResult.Failure(e.sqlState.toErrorCode())
        }

    override fun findHashBy(email: Email): Pair<UserId, Hash>? =
        Users
            .select { Users.email eq email.value }
            .map { UserId(it[Users.id]) to Hash(it[Users.hash]) }
            .singleOrNull()
}