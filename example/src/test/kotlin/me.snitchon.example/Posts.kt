package me.snitchon.example

import me.snitchon.example.ApplicationModule.clock
import me.snitchon.example.ApplicationModule.now
import me.snitchon.example.database.RepositoriesModule.usersRepository
import me.snitchon.example.api.CreatePostRequest
import me.snitchon.example.api.PostResponse
import me.snitchon.example.api.PostsResponse
import me.snitchon.example.api.UserViewResponse
import me.snitchon.example.security.createJWT
import me.snitchon.example.types.*
import me.snitchon.parsers.GsonJsonParser.serialized
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import ro.kreator.aRandom
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC

class Posts : BaseTest() {
    val createPostRequest by aRandom<CreatePostRequest>()
    val user by aRandom<User> {
        copy(
            id = UserId("foo"),
            email = Email("foo@bar.com")
        )
    }
    val validToken by lazy { user.create().let { createJWT(user.id) } }
    val instant = Instant.now()

    init {
        clock.override { Clock.fixed(instant, UTC) }
    }

    @Test
    fun `a logged in user can create a post then view it`() {
        POST("/users/${user.id}/posts")
            .withHeaders(mapOf("X-Access-Token" to validToken))
            .withBody(createPostRequest)
            .expectCode(201)

        GET("/users/${user.id}/posts")
            .withHeaders(mapOf("X-Access-Token" to validToken))
            .expectCode(200)
            .expectBody(
                PostsResponse(
                    listOf(
                        PostResponse(
                            title = createPostRequest.title,
                            content = createPostRequest.content,
                            creator = UserViewResponse(user.id.value, user.name.value),
                            createdAt = now().toString()
                        )
                    )
                ).serialized
            )

    }
}

fun User.create() = transaction {
    usersRepository()
        .putUser(
            CreateUserAction(
                name,
                email,
                Hash(""),
                this@create.id
            )
        )
}