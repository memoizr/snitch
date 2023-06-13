package me.snitchon.example

import com.snitch.me.snitchon.NonEmptyString
import com.snitch.me.snitchon.Validator
import me.snitchon.Router
import me.snitchon.example.CryptoModule.hasher
import me.snitchon.example.UsersRepositoryModule.postsRepository
import me.snitchon.example.UsersRepositoryModule.usersRepository
import me.snitchon.example.api.CreatePostRequest
import me.snitchon.example.api.toResponse
import me.snitchon.example.types.PostContent
import me.snitchon.example.types.PostTitle
import me.snitchon.extensions.print
import me.snitchon.parameters.header
import me.snitchon.parameters.path
import me.snitchon.parsing.Parser
import me.snitchon.request.handle
import me.snitchon.request.parsing
import me.snitchon.service.Endpoint
import org.jetbrains.exposed.sql.transactions.transaction

val userId = path("userId", condition = NonEmptyString)
val accessToken = header("X-Access-Token", condition = ValidAccessToken)

object ValidAccessToken : Validator<String, UserId> {
    override val description = "valid jwt"
    override val regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL)
    override val parse: Parser.(String) -> UserId = {
        try {
            UserId(verifyJWT(it).body.get("userId", String::class.java))
        } catch (e: Exception) {
            e.printStackTrace()
            throw IllegalArgumentException("foo")
        }
    }
}

val usersController: Router.() -> Unit = {
    POST("users")
        .with(body<CreateUserRequest>())
        .isHandledBy(createUser)

    POST("users" / "login")
        .with(body<LoginRequest>())
        .isHandledBy(userLoginHandler)

    POST("users" / userId / "posts")
        .authenticated()
        .with(body<CreatePostRequest>())
        .isHandledBy(createPost)

    GET("users" / userId / "posts")
        .authenticated()
        .isHandledBy(getPosts)
}


private fun <B : Any> Endpoint<B>.authenticated(): Endpoint<B> = copy(
    headerParams = headerParams + accessToken,
    before = {
        it.headers(accessToken.name)
            ?.let { verifyJWT(it) }
    }
)


val getPosts by handle {
    transaction {
        postsRepository().getPosts(request[accessToken])
            .toResponse
    }.ok
}

val createPost by parsing<CreatePostRequest>() handle {
    transaction {
        postsRepository().putPost(
            CreatePostAction(
                request[accessToken],
                PostTitle(body.title),
                PostContent(body.content),
            )
        )
    }
    "Created".created
}

val userLoginHandler by parsing<LoginRequest>() handle {
    transaction { usersRepository().findHashBy(Email(body.email)) }
        .let {
            if (hasher().match(body.password, it?.second ?: Hash(""))) createJWT(it!!.first).ok
            else InvalidCredentials().badRequest
        }
}

private val createUser by parsing<CreateUserRequest>() handle {
    when (
        transaction {
            usersRepository().putUser(
                CreateUserAction(
                    UserName(body.name),
                    Email(body.email),
                    Password(body.password).hash
                )
            )
        }) {
        is TransactionResult.Success -> Unit.created
        is TransactionResult.Failure -> EmailExists().badRequest
    }
}

class InvalidCredentials(val reason: String = "invalid credentials")
class EmailExists(val reason: String = "email already exists")
