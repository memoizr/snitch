package snitch.example.database.repositories.posts

import snitch.example.database.TransactionResult
import snitch.example.database.repositories.Repository
import snitch.example.types.*

interface PostsRepository: Repository {
    fun putPost(post: CreatePostAction): TransactionResult<PostId>
    fun getPosts(userId: UserId): List<Post>
    fun deletePost(userId: UserId, postId: PostId)
    fun getPost(postId: PostId): Post?
    fun updatePost(updatePostAction: UpdatePostAction)
}