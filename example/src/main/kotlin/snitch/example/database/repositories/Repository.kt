package snitch.example.database.repositories

import snitch.example.database.TransactionResult
import snitch.example.database.toErrorCode
import java.sql.SQLException

interface Repository {
    fun <R> tryStatement(statement: () -> R) = try {
        TransactionResult.Success(statement())
    } catch (e: SQLException) {
        e.printStackTrace()
        TransactionResult.Failure<R>(e.sqlState.toErrorCode())
    }
}