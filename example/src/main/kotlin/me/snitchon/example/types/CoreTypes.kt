package me.snitchon.example.types

import java.time.Instant

data class Post(
    val id: PostId,
    val creator: UserView,
    val title: PostTitle,
    val content: PostContent,
    val createdAt: Instant,
)

data class User(val id: UserId, val name: UserName, val email: Email)
data class UserView(val id: UserId, val name: UserName)
