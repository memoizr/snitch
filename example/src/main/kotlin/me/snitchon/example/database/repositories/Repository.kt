package me.snitchon.example.database.repositories

import me.snitchon.example.database.TransactionResult
import me.snitchon.example.database.toErrorCode
import java.sql.SQLException

interface Repository {
    fun <R> tryStatement(statement: () -> R) = try {
        TransactionResult.Success(statement())
    } catch (e: SQLException) {
        e.printStackTrace()
        TransactionResult.Failure<R>(e.sqlState.toErrorCode())
    }
}