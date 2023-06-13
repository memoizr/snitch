package me.snitchon.example

import de.mkammerer.argon2.Argon2Factory
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import life.shank.ShankModule
import life.shank.single
import me.snitchon.example.CryptoModule.privateKey
import me.snitchon.example.CryptoModule.publicKey
import java.security.KeyPairGenerator
import java.security.PublicKey
import java.security.interfaces.RSAPrivateCrtKey
import java.security.interfaces.RSAPublicKey
import java.util.*

object CryptoModule : ShankModule {
    val keyPair = single { ->
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        keyPairGenerator.genKeyPair()
    }

    val privateKey = single { -> keyPair().private as RSAPrivateCrtKey }
    val publicKey = single { -> keyPair().public }
    val hasher = single { -> Hasher(argon()) }
    val argon = single { -> Argon2Factory.create() }
}

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