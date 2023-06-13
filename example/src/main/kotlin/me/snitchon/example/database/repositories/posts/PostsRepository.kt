package me.snitchon.example.database.repositories.posts

import me.snitchon.example.types.CreatePostAction
import me.snitchon.example.types.Post
import me.snitchon.example.types.UserId

interface PostsRepository {
    fun putPost(post: CreatePostAction)
    fun getPosts(userId: UserId): List<Post>
}