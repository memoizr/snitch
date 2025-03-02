package snitch.auth

sealed interface Authentication {
    data class Authenticated(val claims: JWTClaims) : Authentication
    interface Unauthenticated : Authentication
    object InvalidToken : Unauthenticated
    object ExpiredToken : Unauthenticated
    object MissingToken : Unauthenticated
    object InvalidClaims : Unauthenticated
}