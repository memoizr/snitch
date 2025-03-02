package snitch.authorization

import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.MalformedJwtException
import io.jsonwebtoken.SignatureAlgorithm
import java.time.Duration
import java.util.*

class JWTManager {
    fun newToken(claims: JWTClaims) =
        Jwts.builder()
            .setClaims(mapOf("userId" to claims.userId, "role" to claims.role.name))
            .setIssuedAt(Date(SecurityModule.now().toEpochMilli()))
            .setExpiration(Date(SecurityModule.now().toEpochMilli() + Duration.ofMinutes(10).toMillis()))
            .signWith(SecurityModule.privateKey(), SignatureAlgorithm.RS256)
            .compact()

    fun validate(token: String): Authentication =
        try {
            Jwts.parser()
                .setSigningKey(SecurityModule.privateKey())
                .setClock(io.jsonwebtoken.Clock { Date(SecurityModule.now().toEpochMilli()) })
                .parseClaimsJws(token)
                .body.let {
                    Authentication.Authenticated(
                        try {
                            JWTClaims(
                                userId = it["userId"] as String,
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