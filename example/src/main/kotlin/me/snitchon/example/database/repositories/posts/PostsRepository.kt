package me.snitchon.example.database.repositories.posts

import me.snitchon.example.database.TransactionResult
import me.snitchon.example.types.*

interface PostsRepository {
    fun putPost(post: CreatePostAction): TransactionResult<PostId>
    fun getPosts(userId: UserId): List<Post>
    fun deletePost(userId: UserId, postId: PostId)
    fun getPost(postId: PostId): Post?
    fun updatePost(updatePostAction: UpdatePostAction)
}