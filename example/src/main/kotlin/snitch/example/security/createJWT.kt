package snitch.example.security

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import snitch.example.ApplicationModule.now
import snitch.example.security.SecurityModule.privateKey
import snitch.example.types.UserId
import java.time.Duration
import java.util.*

enum class Role {
    ADMIN,
    USER
}

//fun createJWT(userId: UserId, role: Role): String {
//    val jwt = Jwts.builder()
//        .setClaims(mapOf("userId" to userId.value, "role" to role.name))
//        .setIssuedAt(Date())
//        .setExpiration(Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
//        .signWith(privateKey(), SignatureAlgorithm.RS256)
//        .compact()
//
//    return jwt
//}
//
//fun verifyJWT(jwt: String): Jws<Claims> {
//    return Jwts.parserBuilder()
//        .setSigningKey(publicKey())
//        .build()
//        .parseClaimsJws(jwt)
//}

data class TokenClaims(
    val userId: UserId
)

data class JWTClaims(
    val userId: UserId,
    val role: Role
)

sealed interface Authentication {
    data class Authenticated(val claims: JWTClaims) : Authentication
    interface Unauthenticated : Authentication
    object InvalidToken : Unauthenticated
    object ExpiredToken : Unauthenticated
    object MissingToken : Unauthenticated
    object InvalidClaims : Unauthenticated
}

class JWTManager {
    fun newToken(claims: JWTClaims) =
        Jwts.builder()
            .setClaims(mapOf("userId" to claims.userId.value, "role" to claims.role.name))
            .setIssuedAt(Date(now().toEpochMilli()))
            .setExpiration(Date(now().toEpochMilli() + Duration.ofMinutes(10).toMillis()))
            .signWith(privateKey(), SignatureAlgorithm.RS256)
            .compact()

    fun validate(token: String): Authentication =
        try {
            Jwts.parser()
                .setSigningKey(privateKey())
                .setClock(io.jsonwebtoken.Clock { Date(now().toEpochMilli()) })
                .parseClaimsJws(token)
                .body.let {
                    Authentication.Authenticated(
                        try {
                            JWTClaims(
                                userId = UserId(it["userId"] as String),
                                role = Role.valueOf(it["role"] as String),
                            )
                        } catch (e: Exception) {
                            return Authentication.InvalidClaims
                        }
                    )
                }
        } catch (e: MalformedJwtException) {
            Authentication.InvalidToken
        } catch (e: ExpiredJwtException) {
            Authentication.ExpiredToken
        } catch (e: IllegalArgumentException) {
            Authentication.MissingToken
        }
}
