package me.snitchon.example.database

sealed class TransactionResult {
    object Success : TransactionResult()
    class Failure(val code: PostgresErrorCodes?) : TransactionResult()
}