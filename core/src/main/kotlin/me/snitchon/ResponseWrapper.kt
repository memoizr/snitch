package me.snitchon

import me.snitchon.response.Format

interface ResponseWrapper {
    fun setStatus(code: Int)
    fun setType(type: Format)
}