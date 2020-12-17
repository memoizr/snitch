package com.snitch

enum class HTTPMethod {
    GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD;

    companion object {
        fun fromString(method: String) = when (method) {
            DELETE.name -> HTTPMethod.DELETE
            GET.name -> HTTPMethod.GET
            PUT.name -> HTTPMethod.PUT
            POST.name -> HTTPMethod.POST
            OPTIONS.name -> HTTPMethod.OPTIONS
            HEAD.name -> HTTPMethod.HEAD
            PATCH.name -> HTTPMethod.PATCH
            else -> throw IllegalArgumentException(method)
        }
    }
}