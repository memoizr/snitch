package snitch.example.api.users

import snitch.parameters.PathParam
import snitch.request.RequestWrapper
import snitch.request.TypedRequestWrapper
import snitch.request.handling
import snitch.request.parsing
import snitch.router.Router
import snitch.router.decorateWith
import snitch.router.decoration
import snitch.router.routes
import snitch.types.ErrorResponse
import snitch.types.StatusCodes
import org.jetbrains.exposed.sql.transactions.transaction
import snitch.example.api.*
import snitch.example.api.Paths.postId
import snitch.example.api.Paths.userId
import snitch.example.api.auth.authenticated
import snitch.example.api.auth.hasAdminRole
import snitch.example.api.auth.principal
import snitch.example.api.auth.principalEquals
import snitch.example.database.PostgresErrorCodes
import snitch.example.database.RepositoriesModule.postsRepository
import snitch.example.database.RepositoriesModule.usersRepository
import snitch.example.security.JWTClaims
import snitch.example.security.Role
import snitch.example.security.SecurityModule.hasher
import snitch.example.security.SecurityModule.jwt
import snitch.example.types.*

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
    ).noContent
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
            PostgresErrorCodes.UNIQUE_VIOLATION -> EmailExists().badRequest()
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

