package undertow.snitch

import me.snitchon.parsers.GsonJsonParser
import org.junit.Test

class ErrorTest : BaseTest(
    {
        routes {
            GET("errors") inSummary "does a foo" isHandledBy {
                ErrorBody("hellothere", 3f).badRequest()
            }
            GET("exception") inSummary "does a foo" isHandledBy {
                throw CustomException()
                "".ok
            }
        }(it)
            .handleException<CustomException, _> { ex ->
                "Something bad happened".badRequest()
            }
    }
) {

    @Test
    fun `supports typed path parameters`() {
        with(GsonJsonParser) {
            whenPerform GET "/$root/errors" expectCode 400 expectBody (
                ErrorBody(
                    "hellothere",
                    3f
                )
            ).serialized
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