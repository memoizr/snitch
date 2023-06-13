package me.snitchon.example.api

data class CreatePostRequest(
    val content: String,
    val title: String,
)
data class CreateUserRequest(val name: String, val email: String, val password: String)
data class LoginRequest(val email: String, val password: String)
