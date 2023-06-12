package me.snitchon.example

import de.mkammerer.argon2.Argon2Factory
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import life.shank.ShankModule
import life.shank.single
import me.snitchon.example.CryptoModule.privateKey
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateCrtKey
import java.util.*

object CryptoModule : ShankModule {
    val keyPair = single { ->
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        keyPairGenerator.genKeyPair()
    }

    val privateKey = single { -> keyPair().private as RSAPrivateCrtKey }
    val hasher = single { -> Hasher(argon()) }
    val argon = single { -> Argon2Factory.create() }
}

fun createJWT(): String {
    // Generate an RSA key pair

    val claims: MutableMap<String, Any> = HashMap()
    claims["name"] = "John Doe"
    claims["role"] = "admin"

    val jwt = Jwts.builder()
        .setClaims(claims)
        .setIssuedAt(Date())
        .setExpiration(Date(System.currentTimeMillis() + 3600000)) // 1 hour expiration
        .signWith(privateKey(), SignatureAlgorithm.RS256)
        .compact()

    return jwt
}
