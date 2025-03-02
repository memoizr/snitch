package snitch.auth

data class JWTClaims(
    val userId: String,
    val role: Role
)