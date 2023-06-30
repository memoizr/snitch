package snitch.example.api

import snitch.response.ErrorHttpResponse
import snitch.response.HttpResponse
import snitch.response.SuccessfulHttpResponse
import snitch.types.StatusCodes
import snitch.example.database.TransactionResult


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
