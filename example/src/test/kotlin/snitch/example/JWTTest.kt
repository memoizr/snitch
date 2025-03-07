package snitch.example

import com.memoizr.assertk.expect
import com.memoizr.assertk.of
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import org.junit.jupiter.api.Test
import snitch.example.ApplicationModule.clock
import snitch.example.ApplicationModule.now
import snitch.example.security.Authentication
import snitch.example.security.JWTClaims
import snitch.example.security.JWTManager
import snitch.example.security.Role
import snitch.example.security.SecurityModule.privateKey
import snitch.example.types.UserId
import snitch.kofix.aRandom
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
        jwt.validate(token)
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
