package snitch.types

data class ErrorResponse<E>(
    val statusCode: Int,
    val details: E,
)
