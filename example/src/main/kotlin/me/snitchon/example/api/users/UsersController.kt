package me.snitchon.example.api.users

import me.snitchon.Router
import me.snitchon.example.api.*
import me.snitchon.example.security.SecurityModule.hasher
import me.snitchon.example.database.RepositoriesModule.postsRepository
import me.snitchon.example.database.RepositoriesModule.usersRepository
import me.snitchon.example.api.Headers.accessToken
import me.snitchon.example.api.Paths.postId
import me.snitchon.example.api.Paths.userId
import me.snitchon.example.api.auth.authenticated
import me.snitchon.example.api.auth.principal
import me.snitchon.example.database.TransactionResult
import me.snitchon.example.security.createJWT
import me.snitchon.example.types.*
import me.snitchon.extensions.print
import me.snitchon.request.handle
import me.snitchon.request.parsing
import me.snitchon.types.ErrorResponse
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

    DELETE("users" / userId / "posts" / postId)
        .authenticated()
        .isHandledBy(deletePost)

    // get individual post
    GET("users" / userId / "posts" / postId)
        .authenticated()
        .isHandledBy(getPost)

    // update individual post
    PUT("users" / userId / "posts" / postId)
        .authenticated()
        .with(body<UpdatePostRequest>())
        .isHandledBy(updatePost)

}
//updatePost
private val updatePost by parsing<UpdatePostRequest>() handle {
    if (request[userId].print() != principal.value) {
        ErrorResponse(403, "forbidden").forbidden
    } else {
        transaction {
            postsRepository().updatePost(
                UpdatePostAction(
                    PostId(request[postId]),
                    PostTitle(body.title),
                    PostContent(body.content),
                )
            )
        }
        Unit.noContent
    }
}

//getPost
private val getPost by handle {
    transaction {
        postsRepository().getPost(PostId(request[postId]))
            ?.toResponse?.ok
            ?: "notFound".notFound
    }
}

val deletePost by handle {
    if (request[userId].print() != principal.value.print()) {
        ErrorResponse(403, "forbidden").forbidden
    } else {
        transaction {
            postsRepository().deletePost(principal, PostId(request[postId]))
        }.noContent
    }
}

val getPosts by handle {
    transaction {
        postsRepository().getPosts(request[accessToken])
            .toResponse
    }.ok
}

private val createPost by parsing<CreatePostRequest>() handle {
    if (request[userId].print() != principal.value.print()) {
        ErrorResponse(403, "forbidden").forbidden
    } else {
        transaction {
            postsRepository().putPost(
                CreatePostAction(
                    principal,
                    PostTitle(body.title),
                    PostContent(body.content),
                )
            )
        }
        SuccessfulCreation().created
    }
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
        }
    ) {
        is TransactionResult.Success -> SuccessfulCreation().created
        is TransactionResult.Failure -> EmailExists().badRequest
    }
}
