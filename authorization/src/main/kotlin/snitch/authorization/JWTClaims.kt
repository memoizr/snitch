package snitch.authorization

data class JWTClaims(
    val userId: String,
    val role: Role
)