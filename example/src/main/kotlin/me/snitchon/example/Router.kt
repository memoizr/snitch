package me.snitchon.example

import me.snitchon.Router
import me.snitchon.example.CryptoModule.hasher
import me.snitchon.example.UsersRepositoryModule.usersRepository
import me.snitchon.request.Handler
import me.snitchon.types.ErrorResponse

val router: Router.() -> Unit = {
    GET("/health/liveness")
        .isHandledBy { "ok".ok }

    POST("/users")
        .with(body<CreateUserRequest>())
        .isHandledBy(createUserHandler)

    POST("/users/login")
        .with(body<LoginRequest>())
        .isHandledBy(userLoginHandler)
}

val userLoginHandler by Handler<LoginRequest, _, _> {
    val hash = usersRepository().findHashBy(Email(body.email))
    when {
        hash == null -> ErrorResponse(404, "user not found").notFound
        hasher().check(body.password, hash) -> createJWT().ok
        else -> ErrorResponse(400, "wrong password").badRequest
    }
}

val createUserHandler by Handler<CreateUserRequest, _, _> {
    when (usersRepository().putUser(CreateUserAction(body.name, Email(body.email), Password(body.password)))) {
        is TransactionResult.Success -> Unit.noContent
        is TransactionResult.Failure -> ErrorResponse(400, "email already exists").badRequest
    }
}