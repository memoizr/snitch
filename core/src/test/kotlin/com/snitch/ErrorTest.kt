package com.snitch

import me.snitchon.parsers.GsonJsonParser
import org.junit.Test

class ErrorTest : BaseTest(routes {
    GET("errors") inSummary "does a foo" isHandledBy { badRequest<String, ErrorBody>(ErrorBody("hellothere", 3f)) }
}) {
    @Test
    fun `supports typed path parameters`() {
        with (GsonJsonParser) {
            whenPerform GET "/$root/errors" expectCode 400 expectBody badRequest<String, ErrorBody>(
                ErrorBody(
                    "hellothere",
                    3f
                )
            ).jsonString
        }
    }

}

data class ErrorBody(val message: String, val float: Float)
