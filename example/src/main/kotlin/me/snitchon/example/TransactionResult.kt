package me.snitchon.example

sealed class TransactionResult {
    class Success() : TransactionResult()
    class Failure() : TransactionResult()
}