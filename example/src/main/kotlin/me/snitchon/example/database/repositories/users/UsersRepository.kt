package me.snitchon.example.database.repositories.users

import me.snitchon.example.database.TransactionResult
import me.snitchon.example.database.repositories.Repository
import me.snitchon.example.types.CreateUserAction
import me.snitchon.example.types.Email
import me.snitchon.example.types.Hash
import me.snitchon.example.types.UserId

interface UsersRepository: Repository {
    fun putUser(user: CreateUserAction): TransactionResult<UserId>
    fun findHashBy(email: Email): Pair<UserId, Hash>?
}