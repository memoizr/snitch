package snitch.exposed

import java.sql.SQLException

interface Repository {
    fun <R> tryStatement(statement: () -> R) = try {
        TransactionResult.Success(statement())
    } catch (e: SQLException) {
        TransactionResult.Failure(e)
    }
}
