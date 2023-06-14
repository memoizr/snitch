package me.snitchon.example.database.repositories.posts

import me.snitchon.example.ApplicationModule
import me.snitchon.example.database.Posts
import me.snitchon.example.database.Posts.id
import me.snitchon.example.database.TransactionResult
import me.snitchon.example.database.Users
import me.snitchon.example.database.toErrorCode
import me.snitchon.example.types.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.sql.SQLException
import java.util.*

class PostgresPostsRepository() : PostsRepository {
    override fun putPost(post: CreatePostAction): TransactionResult<PostId> =
        try {
            TransactionResult.Success(
                Posts.insert {
                    it[id] = post.id?.value ?: UUID.randomUUID().toString()
                    it[title] = post.title.value
                    it[content] = post.content.value
                    it[creatorId] = post.creator.value
                    it[createdAt] = post.createDate ?: ApplicationModule.now()
                }.let { PostId(it[id]) }
            )
        } catch (e: SQLException) {
            TransactionResult.Failure(e.sqlState.toErrorCode())
        }

    override fun getPosts(userId: UserId) =
        Posts
            .leftJoin(Users)
            .select {
                Posts.creatorId eq userId.value
            }.map {
                Post(
                    PostId(it[Posts.id]),
                    UserView(
                        UserId(it[Users.id]),
                        UserName(it[Users.name]),
                    ),
                    PostTitle(it[Posts.title]),
                    PostContent(it[Posts.content]),
                    it[Posts.createdAt]
                )
            }

    override fun deletePost(userId: UserId, postId: PostId) {
        Posts.deleteWhere {
            id eq postId.value and (creatorId eq userId.value)
        }
    }

    override fun updatePost(updatePostAction: UpdatePostAction) {
        Posts.update({
            id eq updatePostAction.id.value
        }) {
            updatePostAction.title?.value?.let { t -> it[title] = t }
            updatePostAction.content?.value?.let { c -> it[content] = c }
        }
    }

    override fun getPost(postId: PostId): Post? {
        val post = Posts.select {
            id eq postId.value
        }.firstOrNull() ?: return null

        val user = Users.select {
            Users.id eq post[Posts.creatorId]
        }.firstOrNull() ?: return null

        return Post(
            PostId(post[Posts.id]),
            UserView(
                UserId(user[Users.id]),
                UserName(user[Users.name]),
            ),
            PostTitle(post[Posts.title]),
            PostContent(post[Posts.content]),
            post[Posts.createdAt]
        )
    }
}