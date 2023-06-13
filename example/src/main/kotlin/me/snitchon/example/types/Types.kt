package me.snitchon.example.types

import me.snitchon.example.PostId
import me.snitchon.example.UserId
import me.snitchon.example.UserName
import java.time.Instant

data class Post(
    val id: PostId,
    val creator: UserView,
    val title: PostTitle,
    val content: PostContent,
    val createdAt: Instant,
)

data class UserView(
    val id: UserId,
    val name: UserName,
)

@JvmInline
value class PostContent(val value: String)
@JvmInline
value class PostTitle(val value: String)
