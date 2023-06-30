package snitch.example.types

import snitch.example.security.SecurityModule.hasher

@JvmInline
value class PostContent(val value: String)
@JvmInline
value class PostTitle(val value: String)
@JvmInline
value class Hash(val value: String)
@JvmInline
value class PostId(val value: String)
@JvmInline
value class UserId(val value: String)
@JvmInline
value class UserName(val value: String)
@JvmInline
value class Email(val value: String)
@JvmInline
value class Password(val value: String) {
    val hash get() = Hash(hasher().hash(value))
}
