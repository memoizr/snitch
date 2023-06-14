package me.snitchon.example.api

import me.snitchon.example.types.Post
import me.snitchon.example.types.UserView

data class PostsResponse(val posts: List<PostResponse>)
val List<Post>.toResponse get() = PostsResponse(map { it.toResponse })

data class PostResponse(
    val title: String,
    val content: String,
    val creator: UserViewResponse,
    val createdAt: String,
)

val Post.toResponse
    get() = PostResponse(
        title.value,
        content.value,
        creator.toResponse,
        createdAt.toString(),
    )

data class UserViewResponse(
    val id: String,
    val name: String,
)

val UserView.toResponse
    get() = UserViewResponse(
        id = id.value,
        name = name.value
    )

data class SuccessfulCreation(val message: String = "successfully created")

class InvalidCredentials(val reason: String = "invalid credentials")
class EmailExists(val reason: String = "email already exists")
