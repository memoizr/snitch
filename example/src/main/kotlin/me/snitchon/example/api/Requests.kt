package me.snitchon.example.api

data class CreatePostRequest(
    val content: String,
    val title: String,
)