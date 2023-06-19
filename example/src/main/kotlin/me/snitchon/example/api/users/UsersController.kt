package me.snitchon.example.api.users

import me.snitchon.example.api.*
import me.snitchon.example.api.Headers.accessToken
import me.snitchon.example.api.Paths.postId
import me.snitchon.example.api.Paths.userId
import me.snitchon.example.api.auth.authenticated
import me.snitchon.example.api.auth.principal
import me.snitchon.example.api.auth.principalOf
import me.snitchon.example.database.PostgresErrorCodes.UNIQUE_VIOLATION
import me.snitchon.example.database.RepositoriesModule.postsRepository
import me.snitchon.example.database.RepositoriesModule.usersRepository
import me.snitchon.example.security.JWTClaims
import me.snitchon.example.security.Role
import me.snitchon.example.security.SecurityModule.hasher
import me.snitchon.example.security.SecurityModule.jwt
import me.snitchon.example.types.*
import me.snitchon.extensions.print
import me.snitchon.parameters.PathParam
import me.snitchon.request.Context
import me.snitchon.request.RequestWrapper
import me.snitchon.request.handle
import me.snitchon.request.parsing
import me.snitchon.router.routes
import me.snitchon.types.ErrorResponse
import me.snitchon.types.StatusCodes
import org.jetbrains.exposed.sql.transactions.transaction

val usersController = routes {
    POST() with body<CreateUserRequest>() isHandledBy createUser
    POST("login") with body<LoginRequest>() isHandledBy userLogin

    userId / "posts" / {
        authenticated {
            GET() principalOf userId isHandledBy getPosts
            POST() principalOf userId with body<CreatePostRequest>() isHandledBy createPost

            GET(postId) isHandledBy getPost
            PUT(postId).print() principalOf userId with body<UpdatePostRequest>() isHandledBy updatePost
            DELETE(postId) principalOf userId isHandledBy deletePost
        }
    }
}

private val updatePost by parsing<UpdatePostRequest>() handle {
    transaction {
        postsRepository().updatePost(
            UpdatePostAction(
                PostId(request[postId]),
                PostTitle(body.title),
                PostContent(body.content),
            )
        )
    }.noContent
}

private val getPost by handle {
    transaction {
        postsRepository().getPost(PostId(request[postId]))
            ?.toResponse?.ok
            ?: `404`()
    }
}

private val deletePost by handle {
    transaction {
        postsRepository().deletePost(principal, PostId(request[postId]))
    }.noContent
}

private val getPosts by handle {
    transaction {
        postsRepository().getPosts(principal)
            .toResponse.ok
    }
}

private val createPost by parsing<CreatePostRequest>() handle {
    transaction {
        postsRepository().putPost(
            CreatePostAction(
                principal,
                PostTitle(body.title),
                PostContent(body.content),
            )
        )
    }.mapSuccess {
        SuccessfulCreation(value).created
    }.mapFailure {
        FailedCreation().badRequest()
    }
}

private val userLogin by parsing<LoginRequest>() handle {
    transaction { usersRepository().findHashBy(Email(body.email)) }
        ?.let {
            if (hasher().match(body.password, it.second))
                jwt().newToken(JWTClaims(it.first, Role.USER)).ok
            else null
        } ?: InvalidCredentials().badRequest()
}

private val createUser by parsing<CreateUserRequest>() handle {
    println("=====================")
    transaction {
        usersRepository().putUser(
            CreateUserAction(
                UserName(body.name),
                Email(body.email),
                Password(body.password).hash
            )
        )
    }.mapSuccess {
        SuccessfulCreation(value).created
    }.mapFailure {
        when (code) {
            UNIQUE_VIOLATION -> EmailExists().badRequest()
            else -> `500`()
        }
    }
}

fun <T, S : StatusCodes> Context<*>.`403`() =
    ErrorResponse(403, "forbidden").forbidden<T, _, S>()

fun <T, S : StatusCodes> RequestWrapper.`401`() =
    ErrorResponse(401, "unauhtoreized").unauthorized<T, _, S>()

fun <T, S : StatusCodes> Context<*>.`404`() =
    ErrorResponse(404, "forbidden").notFound<T, _, S>()

fun <T, S : StatusCodes> Context<*>.`500`() =
    ServerError().serverError<T, _, S>()

fun Context<*>.principalIsNot(param: PathParam<out Any, *>) =
    request[param] != principal.value
