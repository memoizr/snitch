package me.snitchon.example.api.users

import me.snitchon.example.api.*
import me.snitchon.example.api.Paths.postId
import me.snitchon.example.api.Paths.userId
import me.snitchon.example.api.auth.*
import me.snitchon.example.database.PostgresErrorCodes.UNIQUE_VIOLATION
import me.snitchon.example.database.RepositoriesModule.postsRepository
import me.snitchon.example.database.RepositoriesModule.usersRepository
import me.snitchon.example.security.JWTClaims
import me.snitchon.example.security.Role
import me.snitchon.example.security.SecurityModule.hasher
import me.snitchon.example.security.SecurityModule.jwt
import me.snitchon.example.types.*
import me.snitchon.parameters.PathParam
import me.snitchon.request.TypedRequestWrapper
import me.snitchon.request.RequestWrapper
import me.snitchon.request.handling
import me.snitchon.request.parsing
import me.snitchon.response.HttpResponse
import me.snitchon.router.Router
import me.snitchon.router.routes
import me.snitchon.router.decorateWith
import me.snitchon.router.decoration
import me.snitchon.service.DecoratedWrapper
import me.snitchon.types.ErrorResponse
import me.snitchon.types.StatusCodes
import org.jetbrains.exposed.sql.transactions.transaction

val usersController = routes {
    withTransaction {
        POST() with body<CreateUserRequest>() isHandledBy createUser
        POST("login") with body<LoginRequest>() isHandledBy userLogin

        userId / "posts" / {
            authenticated {
                GET() onlyIf principalEquals(userId) isHandledBy getPosts
                GET() onlyIf principalEquals(userId) isHandledBy getPosts
                POST() onlyIf principalEquals(userId) with body<CreatePostRequest>() isHandledBy createPost

                GET(postId) isHandledBy getPost
                PUT(postId) with body<UpdatePostRequest>() onlyIf principalEquals(userId) isHandledBy updatePost
                DELETE(postId) onlyIf (principalEquals(userId) or hasAdminRole) isHandledBy deletePost
            }
        }
    }
}

val Router.withTransaction get() = decorateWith { transaction { next() } }

private val withExposedTransaction = decoration { transaction { next() } }

private val updatePost by parsing<UpdatePostRequest>() handling {
    postsRepository().updatePost(
        UpdatePostAction(
            PostId(request[postId]),
            PostTitle(body.title),
            PostContent(body.content),
        )
    )
        .noContent
}

private val getPost by handling {
    postsRepository().getPost(PostId(request[postId]))
        ?.toResponse?.ok
        ?: NOT_FOUND()
}

private val deletePost by handling {
    postsRepository().deletePost(request.principal, PostId(request[postId]))
        .noContent
}

private val getPosts by handling {
    postsRepository().getPosts(request.principal)
        .toResponse.ok
}

private val createPost by parsing<CreatePostRequest>() handling {
    postsRepository().putPost(
        CreatePostAction(
            request.principal,
            PostTitle(body.title),
            PostContent(body.content),
        )
    ).mapSuccess {
        SuccessfulCreation(value).created
    }.mapFailure {
        FailedCreation().badRequest()
    }
}

private val userLogin by parsing<LoginRequest>() handling {
    usersRepository().findHashBy(Email(body.email))
        ?.let {
            if (hasher().match(body.password, it.second))
                jwt().newToken(JWTClaims(it.first, Role.USER)).ok
            else null
        } ?: InvalidCredentials().badRequest()
}

private val createUser by parsing<CreateUserRequest>() handling {
    usersRepository().putUser(
        CreateUserAction(
            UserName(body.name),
            Email(body.email),
            Password(body.password).hash
        )
    ).mapSuccess {
        SuccessfulCreation(value).created
    }.mapFailure {
        when (code) {
            UNIQUE_VIOLATION -> EmailExists().badRequest()
            else -> SERVER_ERROR()
        }
    }
}

fun <T, S : StatusCodes> TypedRequestWrapper<*>.FORBIDDEN() =
    ErrorResponse(403, "forbidden").forbidden<T, _, S>()

fun <T, S : StatusCodes> RequestWrapper.FORBIDDEN() =
    ErrorResponse(403, "forbidden").forbidden<T, _, S>()

fun <T, S : StatusCodes> RequestWrapper.UNAUTHORIZED() =
    ErrorResponse(401, "unauthorized").unauthorized<T, _, S>()

fun <T, S : StatusCodes> TypedRequestWrapper<*>.NOT_FOUND() =
    ErrorResponse(404, "forbidden").notFound<T, _, S>()

fun <T, S : StatusCodes> TypedRequestWrapper<*>.SERVER_ERROR() =
    ServerError().serverError<T, _, S>()

fun TypedRequestWrapper<*>.principalIsNot(param: PathParam<out Any, *>) =
    request[param] != request.principal.value

