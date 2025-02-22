package snitch.errors

import snitch.dsl.InlineSnitchTest
import snitch.parsers.GsonJsonParser.serialized
import org.junit.jupiter.api.Test

private data class ErrorBody(val message: String, val float: Float)
private class CustomException : Exception()

class ErrorsTest : InlineSnitchTest() {

    @Test
    fun `supports typed path parameters`() {
        given {
            GET("errors") inSummary "does a foo" isHandledBy { ErrorBody("hellothere", 3f).badRequest() }
        }.then {
            GET("/errors")
                .expectCode(400)
                .expectBody(ErrorBody("hellothere", 3f).serialized)
        }
    }

    @Test
    fun `supports custom exceptions`() {
        given {
            GET("exception") inSummary "does a foo" isHandledBy {
                throw CustomException()
                "".ok
            }
        }.handleException(CustomException::class) {
            "Something bad happened".badRequest()
        }.then {
            GET("/exception")
                .expectCode(400)
                .expect {
                    it.body().contains("Something bad happened")
                }
        }
    }
}