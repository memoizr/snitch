package me.snitchon.errors

import me.snitchon.dsl.InlineSnitchTest
import me.snitchon.parsers.GsonJsonParser.serialized
import org.junit.Test

private data class ErrorBody(val message: String, val float: Float)
private class CustomException : Exception()

class ErrorsTest : InlineSnitchTest() {

    @Test
    fun `supports typed path parameters`() {
        withRoutes {
            GET("errors") inSummary "does a foo" isHandledBy { ErrorBody("hellothere", 3f).badRequest() }
        }.assert {
            GET("/errors")
                .expectCode(400)
                .expectBody(ErrorBody("hellothere", 3f).serialized)
        }
    }

    @Test
    fun `supports custom exceptions`() {
        withRoutes {
            GET("exception") inSummary "does a foo" isHandledBy {
                throw CustomException()
                "".ok
            }
        }.handleException(CustomException::class) {
            "Something bad happened".badRequest()
        }.assert {
            GET("/exception")
                .expectCode(400)
                .expect {
                    it.body().contains("Something bad happened")
                }
        }
    }
}