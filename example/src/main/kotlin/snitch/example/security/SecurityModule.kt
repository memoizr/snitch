package snitch.example.security

import de.mkammerer.argon2.Argon2Factory
import snitch.shank.ShankModule
import snitch.shank.single
import java.security.KeyPairGenerator
import java.security.interfaces.RSAPrivateCrtKey

object SecurityModule : ShankModule {
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