package snitch.exposed

import snitch.response.ErrorHttpResponse
import snitch.response.HttpResponse
import snitch.response.SuccessfulHttpResponse
import snitch.types.StatusCodes
import java.sql.SQLException

sealed class TransactionResult<T> {
    data class Success<T>(val id: T) : TransactionResult<T>()
    class Failure<T>(val e: SQLException?) : TransactionResult<T>()
}

fun <R, T, S : StatusCodes> TransactionResult<R>.mapSuccess(block: R.() -> SuccessfulHttpResponse<T, S>):
        MappedTransactionResult<R, T, S> =
    MappedTransactionResult(this, block)

class MappedTransactionResult<R, T, S : StatusCodes>(
    private val transactionResult: TransactionResult<R>,
    val successMapping: R.() -> SuccessfulHttpResponse<T, S>,
) {
    fun <E> mapFailure(
        failureMapping: TransactionResult.Failure<R>.() -> ErrorHttpResponse<T, out E, S>
    ): HttpResponse<T, S> =
        when (transactionResult) {
            is TransactionResult.Success -> successMapping(transactionResult.id)
            is TransactionResult.Failure -> failureMapping(transactionResult)
        }
}
