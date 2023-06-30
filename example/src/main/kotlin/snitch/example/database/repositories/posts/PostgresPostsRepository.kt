package snitch.example.database.repositories.posts

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import snitch.example.ApplicationModule
import snitch.example.database.Posts
import snitch.example.database.Posts.id
import snitch.example.database.TransactionResult
import snitch.example.database.Users
import snitch.example.types.*
import java.util.*

class PostgresPostsRepository() : PostsRepository {
    override fun putPost(post: CreatePostAction): TransactionResult<PostId> =
        tryStatement {
            Posts.insert {
                it[id] = post.id?.value ?: UUID.randomUUID().toString()
                it[title] = post.title.value
                it[content] = post.content.value
                it[creatorId] = post.creator.value
                it[createdAt] = post.createDate ?: ApplicationModule.now()
            }.let { PostId(it[id]) }
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