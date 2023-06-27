package me.snitchon.example

import com.memoizr.assertk.expect
import com.memoizr.assertk.of
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import life.shank.overrideFactory
import life.shank.resetShank
import me.snitchon.example.ApplicationModule.clock
import me.snitchon.example.ApplicationModule.now
import me.snitchon.example.security.Authentication
import me.snitchon.example.security.JWTClaims
import me.snitchon.example.security.JWTManager
import me.snitchon.example.security.Role
import me.snitchon.example.security.SecurityModule.privateKey
import me.snitchon.example.types.UserId
import me.snitchon.extensions.print
import org.junit.jupiter.api.Test
import ro.kreator.aRandom
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class JWTTest {
    val userId by aRandom<UserId>()
    val jwt = JWTManager()

    @Test
    fun `encodes and decodes a jwt with token`() {
        val token = jwt.newToken(JWTClaims(userId, Role.USER))
        jwt.validate(token).print()
    }

    @Test
    fun `invalid tokens are shown as invalid`() {
        expect that jwt.validate("invalid") isInstance of<Authentication.InvalidToken>()
    }

    @Test
    fun `expired tokens are shown as expired`() {
        val token = jwt.newToken(JWTClaims(userId, Role.USER))

        now.override { Instant.now().plusSeconds(601).truncatedTo(ChronoUnit.MILLIS) }
        expect that jwt.validate(token) isInstance of<Authentication.ExpiredToken>()
        now.override {Instant.now(clock()).truncatedTo(ChronoUnit.MILLIS)}
    }

    @Test
    fun `missing tokens are shown as missing`() {
        expect that jwt.validate("") isInstance of<Authentication.MissingToken>()
    }

    @Test
    fun `invalid claims are shown as invalid`() {
        val token = Jwts.builder()
            .setClaims(mapOf("userId" to "3", "role" to "mary"))
            .setIssuedAt(Date(now().toEpochMilli()))
            .setExpiration(Date(now().toEpochMilli() + Duration.ofMinutes(10).toMillis()))
            .signWith(privateKey(), SignatureAlgorithm.RS256)
            .compact()

        expect that jwt.validate(token) isInstance of<Authentication.InvalidClaims>()
    }
}
