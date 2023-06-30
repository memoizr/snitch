package snitch.example.security

import de.mkammerer.argon2.Argon2
import snitch.example.types.Hash

class PasswordHasher(private val argon: Argon2) {
    fun hash(password: String): String =
        argon.hash(10, 65536, 1, password.toCharArray())

    fun match(password: String, hash: Hash): Boolean =
        argon.verify(hash.value, password.toCharArray())
}