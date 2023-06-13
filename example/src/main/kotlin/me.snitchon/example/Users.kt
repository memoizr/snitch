package me.snitchon.example

import de.mkammerer.argon2.Argon2
import life.shank.ShankModule
import life.shank.new
import life.shank.single
import me.snitchon.example.CryptoModule.hasher
import me.snitchon.example.types.PostContent
import me.snitchon.example.types.PostTitle
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit


data class CreateUserRequest(val name: String, val email: String, val password: String)

data class LoginRequest(val email: String, val password: String)

data class CreateUserAction(val name: UserName, val email: Email, val hash: Hash, val userId: UserId? = null) {
    init {
        if (
            !email.value.isValidEmail()
        ) throw ValidationException("invalid email: ${email.value}")
    }
}

data class CreatePostAction(
    val creator: UserId,
    val title: PostTitle,
    val content: PostContent,
    val createDate: Instant? = null,
    val id: PostId? = null,
)

data class LoginAction(val email: Email, val password: Password)

data class User(val id: UserId, val name: UserName, val email: Email)

@JvmInline
value class Hash(val value: String)

@JvmInline
value class PostId(val value: String)

@JvmInline
value class UserId(val value: String)

@JvmInline
value class UserName(val value: String)

@JvmInline
value class Email(val value: String) {
}

@JvmInline
value class Password(val value: String) {
    val hash get() = Hash(hasher().hash(value))
}

class ValidationException(val reason: String) : Exception()

object ApplicationModule : ShankModule {
    val clock = single { -> Clock.systemUTC() }
    val now = new { -> Instant.now(clock()).truncatedTo(ChronoUnit.MILLIS)}
}

class Hasher(private val argon: Argon2) {
    fun hash(password: String): String =
        argon.hash(10, 65536, 1, password.toCharArray())

    fun match(password: String, hash: Hash): Boolean =
        argon.verify(hash.value, password.toCharArray())
}