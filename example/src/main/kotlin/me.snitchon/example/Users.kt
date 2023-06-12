package me.snitchon.example

import de.mkammerer.argon2.Argon2
import life.shank.ShankModule
import me.snitchon.example.CryptoModule.hasher


data class CreateUserRequest(val name: String, val email: String, val password: String)

data class LoginRequest(val email: String, val password: String)

data class CreateUserAction(val name: String, val email: Email, val password: Password) {
    init {
        if (
            !email.value.isValidEmail()
        ) throw ValidationException("invalid email: ${email.value}")
    }
}
data class LoginAction(val email: Email, val password: Password)

data class User(val id: UserId, val name: UserName, val email: Email)

@JvmInline
value class Hash(val value: String)
@JvmInline
value class UserId(val value: String)
@JvmInline
value class UserName(val value: String)
@JvmInline
value class Email(val value: String) {
}

@JvmInline
value class Password(val value: String) {
    val hash get() = hasher().hash(value)
}

class ValidationException(val reason: String) : Exception()

object ApplicationModule : ShankModule {
}

class Hasher(private val argon: Argon2) {
    fun hash(password: String): String =
        argon.hash(10, 65536, 1, password.toCharArray())

    fun check(password: String, hash: Hash): Boolean =
        argon.verify(hash.value, password.toCharArray())
}