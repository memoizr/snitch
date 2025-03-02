package snitch.authorization

import de.mkammerer.argon2.Argon2

@JvmInline
value class Hash(val value: String)

interface IPasswordHasher {
    fun hash(password: String): String
    fun match(password: String, hash: Hash): Boolean
}

class PasswordHasher(private val argon: Argon2) : IPasswordHasher {
    override fun hash(password: String): String =
        argon.hash(10, 65536, 1, password.toCharArray())

    override fun match(password: String, hash: Hash): Boolean =
        argon.verify(hash.value, password.toCharArray())
}