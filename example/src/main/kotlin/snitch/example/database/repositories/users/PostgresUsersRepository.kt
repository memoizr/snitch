package snitch.example.database.repositories.users

import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import snitch.example.database.Users
import snitch.example.types.CreateUserAction
import snitch.example.types.Email
import snitch.example.types.Hash
import snitch.example.types.UserId
import java.util.*

class PostgresUsersRepository : UsersRepository {

    override fun putUser(user: CreateUserAction) = tryStatement {
        Users.insert {
            it[id] = user.userId?.value ?: UUID.randomUUID().toString()
            it[name] = user.name.value
            it[email] = user.email.value
            it[hash] = user.hash.value
        }.let {
            UserId(it[Users.id])
        }
    }

    override fun findHashBy(email: Email): Pair<UserId, Hash>? =
        Users
            .select { Users.email eq email.value }
            .map { UserId(it[Users.id]) to Hash(it[Users.hash]) }
            .singleOrNull()
}
