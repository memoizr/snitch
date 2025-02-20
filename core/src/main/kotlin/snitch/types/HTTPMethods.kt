package snitch.types

enum class HTTPMethods {
    GET, POST, PUT, PATCH, DELETE, OPTIONS, HEAD;

    companion object {
        fun fromString(method: String) = when (method) {
            GET.name -> GET
            PUT.name -> PUT
            POST.name -> POST
            PATCH.name -> PATCH
            DELETE.name -> DELETE
            OPTIONS.name -> OPTIONS
            HEAD.name -> HEAD
            else -> throw IllegalArgumentException(method)
        }
    }
}