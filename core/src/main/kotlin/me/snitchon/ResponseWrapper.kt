package me.snitchon

import me.snitchon.types.Format

interface ResponseWrapper {
    fun setStatus(code: Int)
    fun setType(type: Format)
}