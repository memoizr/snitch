package me.snitchon.example

interface UsersRepository {
    fun putUser(user: CreateUserAction): TransactionResult
    fun findHashBy(email: Email): Hash?
}