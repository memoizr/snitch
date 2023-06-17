package me.snitchon.example.api

import me.snitchon.example.database.TransactionResult
import me.snitchon.response.ErrorHttpResponse
import me.snitchon.response.HttpResponse
import me.snitchon.response.SuccessfulHttpResponse
import me.snitchon.types.StatusCodes


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
