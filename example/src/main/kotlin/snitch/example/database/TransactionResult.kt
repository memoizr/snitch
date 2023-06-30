package snitch.example.database

sealed class TransactionResult<T> {
    data class Success<T>(val id: T) : TransactionResult<T>()
    class Failure<T>(val code: PostgresErrorCodes?) : TransactionResult<T>()
}