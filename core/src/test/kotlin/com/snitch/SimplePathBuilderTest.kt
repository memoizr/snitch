package com.snitch

import me.snitchon.documentation.Description
import me.snitchon.documentation.Visibility
import me.snitchon.parsers.GsonJsonParser.serialized
import me.snitchon.response.*
import org.junit.Test

class SimplePathBuilderTest : BaseTest(routes {
    GET("foo") inSummary
            "returns a foo" isDescribedAs "" isHandledBy
            { TestResult("get value").ok }

    PUT("/foo") isHandledBy { TestResult("put value").created }
    POST("/foo") isHandledBy { TestResult("post value").created }
    DELETE("/foo") isHandledBy { TestResult("delete value").ok }

    GET("/error") isHandledBy {
        if (false) TestResult("never happens").ok else "Something went wrong".badRequest
    }

    GET("/forbidden") isHandledBy {
        if (false) TestResult("never happens").ok else "Forbidden".forbidden
    }

    GET("noslash/bar") isHandledBy { TestResult("success").ok }
    PUT("noslash/bar") isHandledBy { TestResult("success").ok }
    POST("noslash/bar") isHandledBy { TestResult("success").ok }
    DELETE("noslash/bar") isHandledBy { TestResult("success").ok }

    GET("infixslash" / "bar") isHandledBy { TestResult("success").ok }
    PUT("infixslash" / "bar") isHandledBy { TestResult("success").ok }
    POST("infixslash" / "bar") isHandledBy { TestResult("success").ok }
    DELETE("infixslash" / "bar") isHandledBy { TestResult("success").ok }

    "one" / {
        GET("/a") isHandledBy { TestResult("get value").ok }
        GET("/b") isHandledBy { TestResult("get value").ok }
        "two" / {
            GET("/c") isHandledBy { TestResult("get value").ok }
        }
    }

    "hey" / "there" / {
        GET("/a") isHandledBy { TestResult("get value").ok }
    }

    "v1" / {
        GET() isHandledBy { TestResult("get value").ok }
        GET(clipId) isHandledBy { TestResult("get value").ok }
        GET("one" / clipId) isHandledBy { TestResult("get value").ok }
    }

    GET("params1" / clipId / "params2" / otherPathParam) isHandledBy { TestResult("${request[clipId]}${request[otherPathParam]}").ok }


    GET() isHandledBy { TestResult("get value").ok }


    "hey" / {
        clipId / {
            GET("/a").isHandledBy {
                TestResult("get value").ok
            }
            "level2" / {
                otherPathParam / {
                    "nope" / {
                        GET().isHandledBy {
                            request[clipId]
                            request[otherPathParam]
                            TestResult("get value").ok
                        }
                    }
                }
            }
        }
    }
}
) {


    @Test
    fun `supports nested routes`() {
        whenPerform GET "/$root/hey/123/level2/3459/nope" expectBodyJson TestResult("get value") expectCode 200

        whenPerform GET "/$root/one/a" expectBodyJson TestResult("get value") expectCode 200
        whenPerform GET "/$root/one/b" expectBodyJson TestResult("get value") expectCode 200
        whenPerform GET "/$root/one/two/c" expectBodyJson TestResult("get value") expectCode 200
        whenPerform GET "/$root/hey/there/a" expectBodyJson TestResult("get value") expectCode 200

        whenPerform GET "/$root/hey/a" expectCode 404
        whenPerform GET "/$root/hey/123/a" expectBodyJson TestResult("get value") expectCode 200
        whenPerform GET "/$root/v1/123" expectBodyJson TestResult("get value") expectCode 200
        whenPerform GET "/$root/v1/one/123" expectBodyJson TestResult("get value") expectCode 200
        whenPerform GET "/$root/v1" expectBody TestResult("get value").serialized expectCode 200
        whenPerform GET "/$root" expectBody TestResult("get value").serialized expectCode 200

    }

    @Test
    fun `returns successful status codes`() {
        whenPerform GET "/$root/foo" expectBodyJson TestResult("get value") expectCode 200
        whenPerform PUT "/$root/foo" expectBodyJson TestResult("put value") expectCode 201
        whenPerform POST "/$root/foo" expectBodyJson TestResult("post value") expectCode 201
        whenPerform DELETE "/$root/foo" expectBodyJson TestResult("delete value") expectCode 200
    }

    @Test
    fun `returns error responses`() {
        whenPerform GET "/$root/error" expectBody """"Something went wrong"""" expectCode 400
        whenPerform GET "/$root/forbidden" expectBody """"Forbidden"""" expectCode 403
    }

    @Test
    fun `when there's no leading slash, it adds it`() {
        whenPerform GET "/$root/noslash/bar" expectBodyJson TestResult("success") expectCode 200
        whenPerform PUT "/$root/noslash/bar" expectBodyJson TestResult("success") expectCode 200
        whenPerform POST "/$root/noslash/bar" expectBodyJson TestResult("success") expectCode 200
        whenPerform DELETE "/$root/noslash/bar" expectBodyJson TestResult("success") expectCode 200
    }

    @Test
    fun `supports infix slash`() {
        whenPerform GET "/$root/infixslash/bar" expectBodyJson TestResult("success") expectCode 200
        whenPerform PUT "/$root/infixslash/bar" expectBodyJson TestResult("success") expectCode 200
        whenPerform POST "/$root/infixslash/bar" expectBodyJson TestResult("success") expectCode 200
        whenPerform DELETE "/$root/infixslash/bar" expectBodyJson TestResult("success") expectCode 200
    }

    @Test
    fun `supports several path parameters`() {
        whenPerform GET "/$root/params1/90/params2/32" expectBodyJson TestResult("9032") expectCode 200
//        whenPerform GET "/$root/params3/42/params4/24" expectBodyJson TestResult("4224") expectCode 200
    }
}

data class TestResult(@Description(visibility = Visibility.INTERNAL) val value: String)
