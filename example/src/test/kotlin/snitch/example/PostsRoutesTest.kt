package snitch.example

import me.snitch.parsers.GsonJsonParser.serialized
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ro.kreator.aRandom
import snitch.example.ApplicationModule.clock
import snitch.example.ApplicationModule.now
import snitch.example.api.*
import snitch.example.database.RepositoriesModule.postsRepository
import snitch.example.database.RepositoriesModule.usersRepository
import snitch.example.security.JWTClaims
import snitch.example.security.Role
import snitch.example.security.SecurityModule.jwt
import snitch.example.types.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset.UTC

class PostsRoutesTest : BaseTest() {
    val createPostRequest by aRandom<CreatePostRequest>()
    val updatePostRequest by aRandom<UpdatePostRequest>()

    val otherUser by aRandom<User>()
    val user by aRandom<User>()
    val admin by aRandom<User>()
    val post by aRandom<Post> { copy(creator = UserView(user.id, user.name), createdAt = now()) }
    val postByOtherUser by aRandom<Post> { copy(creator = UserView(otherUser.id, otherUser.name), createdAt = now()) }

    lateinit var userToken: String
    lateinit var adminToken: String

    @BeforeEach
    fun setup() {
        clock.override { Clock.fixed(Instant.now(), UTC) }
        userToken = user.create().let { jwt().newToken(JWTClaims(user.id, Role.USER)) }
        adminToken = admin.create().let { jwt().newToken(JWTClaims(admin.id, Role.ADMIN)) }

        otherUser.create()
    }

    @AfterEach
    fun teardown() {
        clock.override(null)
    }

    @Test
    fun `a logged in user can create a post then view it`() {
        POST("/users/${user.id.value}/posts")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .withBody(createPostRequest)
            .expectCode(201)

        GET("/users/${user.id.value}/posts")
            .withHeaders(mapOf("X-Access-Token" to userToken))
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

    @Test
    fun `a user with a missing or invalid token is not allowed to post`() {
        POST("/users/${user.id.value}/posts")
            .withHeaders(mapOf("X-Access-Token" to "bad token"))
            .withBody(createPostRequest)
            .expectCode(401)

        POST("/users/${user.id.value}/posts")
            .withBody(createPostRequest)
            .expectCode(400)
    }

    @Test
    fun `an user cannot post on another user's behalf`() {
        POST("/users/${otherUser.id.value}/posts")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .withBody(createPostRequest)
            .expectCode(403)
    }

    @Test
    fun `a logged in user can delete his own post`() {
        post.create()

        DELETE("/users/${user.id.value}/posts/${post.id}")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .expectCode(204)
    }

    @Test
    fun `a logged in user cannot delete another user's post`() {
        postByOtherUser.create()

        DELETE("/users/${otherUser.id.value}/posts/${postByOtherUser.id}")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .expectCode(403)
    }

    @Test
    fun `a logged in admin can delete another user's post`() {

        DELETE("/users/${otherUser.id.value}/posts/${postByOtherUser.id}")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .expectCode(403)
    }

    @Test
    fun `a logged in user can get individual post`() {
        post.create()

        GET("/users/${user.id.value}/posts/${post.id.value}")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .expectCode(200)
            .expectBody(
                PostResponse(
                    title = post.title.value,
                    content = post.content.value,
                    creator = UserViewResponse(user.id.value, user.name.value),
                    createdAt = now().toString()
                ).serialized
            )
    }

    @Test
    fun `a logged in user cannot get individual post if it does not exist`() {
        GET("/users/${user.id.value}/posts/${post.id.value}")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .expectCode(404)
    }

    @Test
    fun `a logged in user can update his own post`() {
        post.create()
        PUT("/users/${user.id.value}/posts/${post.id.value}")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .withBody(updatePostRequest)
            .expectCode(204)

        // get post to verify it was updated
        GET("/users/${user.id.value}/posts/${post.id.value}")
            .withHeaders(mapOf("X-Access-Token" to userToken))
            .expectCode(200)
            .expectBody(
                PostResponse(
                    title = updatePostRequest.title,
                    content = updatePostRequest.content,
                    creator = UserViewResponse(user.id.value, user.name.value),
                    createdAt = now().toString()
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

fun Post.create() = transaction {
    postsRepository()
        .putPost(
            CreatePostAction(
                creator.id,
                title,
                content,
                now(),
                this@create.id
            )
        )
}
