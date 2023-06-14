package me.snitchon.example.types

import java.time.Instant

data class CreateUserAction(val name: UserName, val email: Email, val hash: Hash, val userId: UserId? = null) {
    init {
        if (
            !email.value.isValidEmail()
        ) throw ValidationException("invalid email: ${email.value}")
    }
}

private fun String.isValidEmail(): Boolean {
    val emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    return emailRegex.matches(this)
}

data class CreatePostAction(
    val creator: UserId,
    val title: PostTitle,
    val content: PostContent,
    val createDate: Instant? = null,
    val id: PostId? = null,
)

data class UpdatePostAction(
    val id: PostId,
    val title: PostTitle? = null,
    val content: PostContent? = null,
)

data class LoginAction(val email: Email, val password: Password)
