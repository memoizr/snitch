package me.snitchon.example.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import me.snitchon.example.security.SecurityModule.privateKey
import me.snitchon.example.security.SecurityModule.publicKey
import me.snitchon.example.types.UserId
import java.util.*

fun createJWT(userId: UserId): String {
    val jwt = Jwts.builder()
        .setClaims(mapOf("userId" to userId.value))
        .setIssuedAt(Date())
        .setExpiration(Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
        .signWith(privateKey(), SignatureAlgorithm.RS256)
        .compact()

    return jwt
}

fun verifyJWT(jwt: String): Jws<Claims> {
    return Jwts.parserBuilder()
        .setSigningKey(publicKey())
        .build()
        .parseClaimsJws(jwt)
}

data class TokenClaims(
    val userId: UserId
)