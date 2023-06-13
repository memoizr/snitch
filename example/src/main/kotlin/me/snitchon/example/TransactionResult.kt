package me.snitchon.example

import me.snitchon.example.repository.PostgresErrorCodes

sealed class TransactionResult {
    object Success : TransactionResult()
    class Failure(val code: PostgresErrorCodes?) : TransactionResult()
}