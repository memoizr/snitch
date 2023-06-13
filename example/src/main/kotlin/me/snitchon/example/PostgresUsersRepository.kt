package me.snitchon.example

import me.snitchon.example.ApplicationModule.now
import me.snitchon.example.repository.Posts
import me.snitchon.example.repository.Users
import me.snitchon.example.repository.toErrorCode
import me.snitchon.example.types.Post
import me.snitchon.example.types.PostContent
import me.snitchon.example.types.PostTitle
import me.snitchon.example.types.UserView
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.sql.SQLException
import java.time.Clock
import java.time.Instant
import java.util.*

class PostgresUsersRepository : UsersRepository {

    override fun putUser(user: CreateUserAction) =
        try {
            Users.insert {
                it[id] = user.userId?.value ?: UUID.randomUUID().toString()
                it[name] = user.name.value
                it[email] = user.email.value
                it[hash] = user.hash.value
            }
            TransactionResult.Success
        } catch (e: SQLException) {
            TransactionResult.Failure(e.sqlState.toErrorCode())
        }.also {
            println("=======================================")
            println(it)
        }

    override fun findHashBy(email: Email): Pair<UserId, Hash>? =
        Users
            .select { Users.email eq email.value }
            .map { UserId(it[Users.id]) to Hash(it[Users.hash]) }
            .singleOrNull()
}

class PostsRepository() {
    fun putPost(post: CreatePostAction) {
        Posts.insert {
            it[id] = post.id?.value ?: UUID.randomUUID().toString()
            it[title] = post.title.value
            it[content] = post.content.value
            it[creatorId] = post.creator.value
            it[createdAt] = post.createDate ?: now()
        }
    }

    fun getPosts(userId: UserId) =
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
}