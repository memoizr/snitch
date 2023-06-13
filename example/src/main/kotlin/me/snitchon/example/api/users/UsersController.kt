package me.snitchon.example.api.users

import me.snitchon.Router
import me.snitchon.example.security.SecurityModule.hasher
import me.snitchon.example.database.RepositoriesModule.postsRepository
import me.snitchon.example.database.RepositoriesModule.usersRepository
import me.snitchon.example.api.CreatePostRequest
import me.snitchon.example.api.CreateUserRequest
import me.snitchon.example.api.Headers.accessToken
import me.snitchon.example.api.LoginRequest
import me.snitchon.example.api.Paths.userId
import me.snitchon.example.api.auth.authenticated
import me.snitchon.example.api.toResponse
import me.snitchon.example.database.TransactionResult
import me.snitchon.example.security.createJWT
import me.snitchon.example.types.*
import me.snitchon.request.handle
import me.snitchon.request.parsing
import org.jetbrains.exposed.sql.transactions.transaction


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

val getPosts by handle {
    transaction {
        postsRepository().getPosts(request[accessToken])
            .toResponse
    }.ok
}

private val createPost by parsing<CreatePostRequest>() handle {
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

private val userLoginHandler by parsing<LoginRequest>() handle {
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
