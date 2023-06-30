package me.snitch.types

enum class HTTPMethods {
    GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD;

    companion object {
        fun fromString(method: String) = when (method) {
            DELETE.name -> DELETE
            GET.name -> GET
            PUT.name -> PUT
            POST.name -> POST
            OPTIONS.name -> OPTIONS
            HEAD.name -> HEAD
            PATCH.name -> PATCH
            else -> throw IllegalArgumentException(method)
        }
    }
}