package snitch.example.database.repositories.users

import snitch.example.database.TransactionResult
import snitch.example.database.repositories.Repository
import snitch.example.types.CreateUserAction
import snitch.example.types.Email
import snitch.example.types.Hash
import snitch.example.types.UserId

interface UsersRepository: Repository {
    fun putUser(user: CreateUserAction): TransactionResult<UserId>
    fun findHashBy(email: Email): Pair<UserId, Hash>?
}