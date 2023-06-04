package com.snitch

import me.snitchon.parsers.GsonJsonParser
import org.junit.Test

class ErrorTest : BaseTest(
    {
        routes {
            GET("errors") inSummary "does a foo" isHandledBy {
                badRequest<String, ErrorBody>(ErrorBody("hellothere", 3f))
            }
            GET("exception") inSummary "does a foo" isHandledBy {
                throw CustomException()
                "".ok
            }
        }(it)
            .handleException<CustomException, _> { ex, req ->
                HttpResponse.ErrorHttpResponse<String, String>(400, "Something bad happened")
            }
    }
) {
    @Test
    fun `supports typed path parameters`() {
        with(GsonJsonParser) {
            whenPerform GET "/$root/errors" expectCode 400 expectBody badRequest<String, ErrorBody>(
                ErrorBody(
                    "hellothere",
                    3f
                )
            ).jsonString
        }
    }

    @Test
    fun `supports custom exceptions`() {
        GET("/$root/exception")
            .expectCode(400)
            .expect {
                it.body().contains("Something bad happened")
            }
    }

}

data class ErrorBody(val message: String, val float: Float)

class CustomException : Exception()