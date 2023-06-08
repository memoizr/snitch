package me.snitchon

interface ResponseWrapper {
    fun setStatus(code: Int)
    fun setType(type: Format)
}