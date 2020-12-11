package com.snitch

import com.snitch.extensions.json
import org.junit.Rule
import org.junit.Test

class ErrorTest : SparkTest() {
    @Rule
    @JvmField
    val rule = SparkTestRule(port) {
        GET("errors")inSummary "does a foo" isHandledBy { badRequest<String, ErrorBody>(ErrorBody("hellothere", 3f)) }
    }

    @Test
    fun `supports typed path parameters`() {
        whenPerform GET "/$root/errors" expectCode 400 expectBody badRequest<String, ErrorBody>(ErrorBody("hellothere", 3f)).json
    }

}

data class ErrorBody(val message: String, val float: Float)
