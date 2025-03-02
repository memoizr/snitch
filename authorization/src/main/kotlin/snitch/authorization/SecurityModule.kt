package snitch.authorization

import de.mkammerer.argon2.Argon2Factory
import snitch.shank.ShankModule
import snitch.shank.new
import snitch.shank.single
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateCrtKey
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

object SecurityModule : ShankModule {
    val clock = single { -> Clock.systemUTC() }
    val now = new { -> Instant.now(clock()).truncatedTo(ChronoUnit.MILLIS) }
    val keyPair = single { ->
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        keyPairGenerator.genKeyPair()
    }

    val privateKey = single { -> keyPair().private as RSAPrivateCrtKey }
    val publicKey = single { -> keyPair().public }
    val hasher = single<IPasswordHasher> { -> PasswordHasher(argon()) }
    val argon = single { -> Argon2Factory.create() }
    val jwt = single { -> JWTManager() }
}