package me.snitchon.example

import me.snitchon.types.StatusCodes.NO_CONTENT
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import ro.kreator.aRandom

class CreatesNewUser : BaseTest() {
    val createRequest by aRandom<CreateUserRequest> { copy(email = "foo@gmail.com") }
    val otherRequestSameEmail by aRandom<CreateUserRequest> { copy(email = createRequest.email) }

    @Test
    fun `when an user with same email does not exist creates user`() {
        POST("/users")
            .withBody(createRequest)
            .expectCode(NO_CONTENT.code)
    }

    @Test
    fun `when creating user with same email as other user returns error`() {
        POST("/users")
            .withBody(createRequest)
            .expectCode(NO_CONTENT.code)

        POST("/users")
            .withBody(otherRequestSameEmail)
            .expectCode(400)
            .expect {
                Assertions.assertThat(it.body()).contains("already exists").contains("email")
            }
    }

    @Test
    fun `when creating user with invaild email returns error`() {
        POST("/users")
            .withBody(createRequest.copy(email = "foobar"))
            .expectCode(400)
            .expect {
                Assertions.assertThat(it.body()).contains("invalid").contains("email")
            }
    }

    @Test
    fun `after creating a user it allows the user to login by email and password`() {
        POST("/users")
            .withBody(createRequest)
            .expectCode(NO_CONTENT.code)

        POST("/users/login")
            .withBody(LoginRequest(email = createRequest.email, password = createRequest.password))
            .expectCode(200)
    }
}