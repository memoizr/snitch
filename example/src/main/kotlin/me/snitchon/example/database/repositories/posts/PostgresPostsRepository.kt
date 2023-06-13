package me.snitchon.example.database.repositories.posts

import me.snitchon.example.ApplicationModule
import me.snitchon.example.database.Posts
import me.snitchon.example.database.Users
import me.snitchon.example.types.*
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import java.util.*

class PostgresPostsRepository() : PostsRepository {
    override fun putPost(post: CreatePostAction) {
        Posts.insert {
            it[Posts.id] = post.id?.value ?: UUID.randomUUID().toString()
            it[Posts.title] = post.title.value
            it[Posts.content] = post.content.value
            it[Posts.creatorId] = post.creator.value
            it[Posts.createdAt] = post.createDate ?: ApplicationModule.now()
        }
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
}