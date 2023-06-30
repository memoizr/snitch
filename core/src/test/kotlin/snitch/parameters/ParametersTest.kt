package snitch.parameters

import snitch.validation.ofNonEmptySingleLineString
import snitch.validation.ofNonNegativeInt
import snitch.documentation.Visibility
import snitch.dsl.InlineSnitchTest
import snitch.parsers.GsonJsonParser.serialized
import snitch.parsing.Parser
import snitch.request.parsing
import snitch.types.Sealed
import snitch.types.ErrorResponse
import snitch.validation.Validator
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

val stringParam by path(ofNonEmptySingleLineString)
val intParam by path(ofNonNegativeInt)
val q by query()
val int by query(ofNonNegativeInt, emptyAsMissing = true)
private val offset by optionalQuery(ofNonNegativeInt, default = 30)
val limit by optionalQuery(ofNonNegativeInt)

val qHead by header(ofNonEmptySingleLineString, name = "q")
val intHead by header(ofNonNegativeInt, name = "int")
val offsetHead by optionalHeader(ofNonNegativeInt, emptyAsMissing = true, default = 666)
val limitHead by optionalHeader(ofNonNegativeInt, emptyAsMissing = true)
val queryParam by optionalQuery(ofNonEmptySingleLineString, name = "param", default = "hey")
val headerParam by optionalHeader(
    ofNonEmptySingleLineString,
    name = "param",
    default = "hey",
    visibility = Visibility.INTERNAL
)
val pathParam by path(ofNonEmptySingleLineString, name = "param")
val time by query(DateValidator)

object DateValidator : Validator<Date, Date> {
    override val description: String = "An iso 8601 format date"
    override val regex: Regex =
        """^(?:[1-9]\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\d|2[0-3]):[0-5]\d:[0-5]\d(?:Z|[+-][01]\d:[0-5]\d)$""".toRegex()
    override val parse: Parser.(Collection<String>) -> Date = {
        it.first().let {
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(it)
        }
    }
}

data class IntTestResult(val result: Int)
data class NullableIntTestResult(val result: Int?)
data class DateResult(val date: Date)
data class BodyParam(val int: Int, val string: String, val sealed: SealedClass)
data class BodyTestResult(val a: Int, val b: Int)
data class TestErrorHttpResponse<T, E>(val statusCode: Int, val details: E)
sealed class SealedClass : Sealed() {
    data class One(val oneInt: Int) : SealedClass()
    object Two : SealedClass()
}

data class TestResult(val string: String)


class ParametersTest : InlineSnitchTest() {

    @Test
    fun `supports typed path parameters`() {
        given {
            GET("stringpath" / stringParam) isHandledBy { TestResult(request[stringParam]).ok }
            GET("intpath" / intParam) isHandledBy { IntTestResult(request[intParam]).ok }
        } then {
            GET("/stringpath/hellothere").expectBodyJson(TestResult("hellothere"))
            GET("/intpath/300").expectBodyJson(IntTestResult(300))
        }
    }


    @Test
    fun `validates path parameters`() {
        given {
            GET("intpath2" / intParam / "end") isHandledBy {
                IntTestResult(request[intParam]).ok
            }
        } then {
            GET("/intpath2/4545/end").expectBody(IntTestResult(4545).serialized)
            GET("/intpath2/hello/end").expectBody(
                ErrorResponse(
                    400,
                    listOf("Path parameter `intParam` is invalid, expecting non negative integer, got `hello`")
                ).serialized
            )
        }
    }

    @Test
    fun `supports query parameters`() {
        given {
            GET("queriespath") inSummary "does a foo" withQuery q isHandledBy { TestResult(request[q]).ok }
            GET("queriespath2") with queries(int) isHandledBy { IntTestResult(request[int]).ok }
        } then {
            GET("/queriespath?q=foo").expectBodyJson(TestResult("foo"))
            GET("/queriespath?q=foo%0Abar").expectBodyJson(TestResult("foo\nbar"))
            GET("/queriespath?q=").expectBodyJson(
                TestErrorHttpResponse<TestResult, List<String>>(
                    400,
                    listOf("Query parameter `q` is invalid, expecting non empty string, got ``")
                )
            )
            GET("/queriespath").expectBodyJson(
                TestErrorHttpResponse<TestResult, List<String>>(
                    400,
                    listOf("Required Query parameter `q` is missing")
                )
            )

            GET("/queriespath2?int=3434").expectBodyJson(IntTestResult(3434))
            GET("/queriespath2?int=").expectBodyJson(
                TestErrorHttpResponse<TestResult, List<String>>(
                    400,
                    listOf("Required Query parameter `int` is missing")
                )
            )
            GET("/queriespath2?int=hello").expectBodyJson(
                TestErrorHttpResponse<TestResult, List<String>>(
                    400,
                    listOf("Query parameter `int` is invalid, expecting non negative integer, got `hello`")
                )
            )
            GET("/queriespath2?int=-34").expectBodyJson(
                TestErrorHttpResponse<TestResult, List<String>>(
                    400,
                    listOf("Query parameter `int` is invalid, expecting non negative integer, got `-34`")
                )
            )
        }
    }


    @Test
    fun `supports default values for query parameters`() {
        given {
            GET("queriespath3") with queries(offset) isHandledBy { IntTestResult(request[offset]).ok }
            GET("queriespath4") with queries(limit) isHandledBy { NullableIntTestResult(request[limit]).ok }
        } then {
            GET("/queriespath3?offset=42").expectBody(IntTestResult(42).serialized)
            GET("/queriespath3").expectBody(IntTestResult(30).serialized)
            GET("/queriespath4?limit=42").expectBody(NullableIntTestResult(42).serialized)
            GET("/queriespath4").expectBody("""{}""")
        }
    }

    @Test
    fun `supports header parameters`() {
        given {
            GET("headerspath") with headers(qHead) isHandledBy { TestResult(request[qHead]).ok }
            GET("headerspath2") with headers(intHead) isHandledBy { IntTestResult(request[intHead]).ok }
        } then {
            GET("/headerspath").withHeaders(mapOf(qHead.name to "foo")).expectBodyJson(TestResult("foo"))
            GET("/headerspath").withHeaders(mapOf(qHead.name to "")).expectBody(
                TestErrorHttpResponse<TestResult, List<String>>(
                    400,
                    listOf("Header parameter `q` is invalid, expecting non empty single-line string, got ``")
                ).serialized
            )
            GET("/headerspath").withHeaders(mapOf()).expectBodyJson(
                TestErrorHttpResponse<TestResult, List<String>>(
                    400,
                    listOf("Required Header parameter `q` is missing")
                )
            )

            GET("/headerspath2").withHeaders(mapOf(intHead.name to 3434)).expectBodyJson(IntTestResult(3434))
            GET("/headerspath2").expectBodyJson(
                TestErrorHttpResponse<TestResult, List<String>>(
                    400,
                    listOf("Required Header parameter `int` is missing")
                )
            )
            GET("/headerspath2").withHeaders(mapOf(intHead.name to "hello")).expectBodyJson(
                TestErrorHttpResponse<TestResult, List<String>>(
                    400,
                    listOf("Header parameter `int` is invalid, expecting non negative integer, got `hello`")
                )
            )
            GET(
                "/headerspath2"
            ).withHeaders(mapOf(intHead.name to -34)).expectBodyJson(
                TestErrorHttpResponse<TestResult, List<String>>(
                    400,
                    listOf("Header parameter `int` is invalid, expecting non negative integer, got `-34`")
                )
            )
        }
    }

    @Test
    fun `supports default values for header parameters`() {
        given {
            GET("headerspath3") with headers(offsetHead) isHandledBy { NullableIntTestResult(request[offsetHead]).ok }
            GET("headerspath4") with headers(limitHead) isHandledBy { NullableIntTestResult(request[limitHead]).ok }
        } then {
            GET("/headerspath3").withHeaders(mapOf(offsetHead.name to 42))
                .expectBody(IntTestResult(42).serialized)
            GET("/headerspath3").expectBody(IntTestResult(666).serialized)
            GET("/headerspath3").expectBody(IntTestResult(666).serialized)

            GET("/headerspath4").withHeaders(mapOf(limitHead.name to 42)).expectBody(
                NullableIntTestResult(
                    42
                ).serialized
            )
            GET("/headerspath4").withHeaders(mapOf(limitHead.name to "")).expectBody("""{}""")
            GET("/headerspath4").expectBody("""{}""")
        }
    }


    @Test
    fun `supports custom parsing`() {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        val date = "2018-06-30T02:59:51-00:00"
        given {
            GET("customParsing") with queries(time) isHandledBy { DateResult(request[time]).ok }
        } then {
            GET("/customParsing?time=$date").expectBody(DateResult(df.parse(date)).serialized)
        }
    }


    @Test
    fun `forbids using parameters which aren't registered`() {
        given {
            GET("sneakyqueryparams") isHandledBy { TestResult(request.get(queryParam)).ok }
            GET("sneakyheaderparams") isHandledBy { TestResult(request.get(headerParam)).ok }
            GET("sneakypathparams" / pathParam.copy(name = "sneaky")) isHandledBy { TestResult(request.get(pathParam)).ok }
        } then {
            GET("/sneakyqueryparams").expectBody(
                ErrorResponse(
                    500,
                    "Attempting to use unregistered query parameter `param`"
                ).serialized
            )
            GET("/sneakyheaderparams").expectBody(
                ErrorResponse(
                    500,
                    "Attempting to use unregistered header parameter `param`"
                ).serialized
            )
            GET("/sneakypathparams/343").expectBody(
                ErrorResponse(
                    500,
                    "Attempting to use unregistered path parameter `param`"
                ).serialized
            )
        }
    }

    @Test
    fun `supports body parameter`() {
        val bodyParam = BodyParam(42, "hello", SealedClass.One(33))
        val function by parsing<BodyParam>() handling {
            val sealed = body.sealed
            BodyTestResult(
                body.int, when (sealed) {
                    is SealedClass.One -> sealed.oneInt
                    is SealedClass.Two -> 2
                }
            ).ok
        }

        given {
            POST("bodyparam") with body<BodyParam>() isHandledBy function
        } then {
            POST("/bodyparam").withBody(bodyParam).expectBody(BodyTestResult(42, 33).serialized)
        }
    }

    @Test
    fun `returns error for failed parsing of body parameter`() {
        given {
            POST("bodyparam") with body<BodyParam>() isHandledBy { body.sealed.ok }
        } then {
            POST("/bodyparam").withBody("lolol").expectCode(400)
                .expectBody("""{"statusCode":400,"details":"Invalid body parameter"}""")
        }
    }

    @Test
    fun `supports a optional header parameters`() {
        val param by optionalHeader(ofNonNegativeInt)
        given {
            GET() withHeader param isHandledBy { (request[param] ?: 0).ok }
        } then {
            GET("/").withHeader("param" to "4").expectBody("4")
            GET("/").expectBody("0")
        }
    }

    @Test
    fun `supports a optional header parameter with default`() {
        val param by optionalHeader(
            ofNonNegativeInt,
            default = 5,
            emptyAsMissing = true,
            invalidAsMissing = true
        )

        given {
            GET() withHeader param isHandledBy { (request[param]).ok }
        } then {
            GET("/")
                .withHeader("param" to "4")
                .expectBody("4")
            GET("/")
                .withHeader("param" to "foo")
                .expectBody("5")
            GET("/")
                .withHeader("param" to "")
                .expectBody("5")
            GET("/").expectBody("5")
        }
    }

    @Test
    fun `supports a optional header parameter without default`() {
        val param by optionalHeader(
            ofNonNegativeInt,
            emptyAsMissing = true,
            invalidAsMissing = true
        )

        given {
            GET() withHeader param isHandledBy { ((request[param]) ?: 5).ok }
        } then {
            GET("/")
                .withHeader("param" to "4")
                .expectBody("4")
            GET("/")
                .withHeader("param" to "foo")
                .expectBody("5")
            GET("/")
                .withHeader("param" to "")
                .expectBody("5")
            GET("/").expectBody("5")
        }
    }

    @Test
    fun `supports a optional query parameter with default`() {
        val param by optionalQuery(
            ofNonNegativeInt,
            default = 5,
            emptyAsMissing = true,
            invalidAsMissing = true
        )

        given {
            GET() withQuery param isHandledBy { (request[param]).ok }
        } then {
            GET("/?param=4")
                .expectBody("4")
            GET("/?param=foo")
                .expectBody("5")
            GET("/?param=")
                .expectBody("5")
            GET("/").expectBody("5")
        }
    }

    @Test
    fun `supports a optional query parameter without default`() {
        val param by optionalQuery(
            ofNonNegativeInt,
            emptyAsMissing = true,
            invalidAsMissing = true
        )

        given {
            GET() withQuery param isHandledBy { (request[param] ?: 42).ok }
        } then {
            GET("/?param=4")
                .expectBody("4")
            GET("/?param=foo")
                .expectBody("42")
            GET("/?param=")
                .expectBody("42")
            GET("/").expectBody("42")
        }
    }


    @Test
    fun `supports a optional string header parameter with default`() {
        val param by optionalHeader(
            default = "5",
            emptyAsMissing = true,
        )

        given {
            GET() withHeader param isHandledBy { (request[param]).ok }
        } then {
            GET("/")
                .withHeader("param" to "4")
                .expectBody(""""4"""")
            GET("/")
                .withHeader("param" to "")
                .expectBody(""""5"""")
            GET("/").expectBody(""""5"""")
        }
    }

    @Test
    fun `supports a optional string header parameter without default`() {
        val param by optionalHeader(
            emptyAsMissing = true,
        )

        given {
            GET() withHeader param isHandledBy { ((request[param]) ?: "5").ok }
        } then {
            GET("/")
                .withHeader("param" to "4")
                .expectBody(""""4"""")
            GET("/")
                .withHeader("param" to "")
                .expectBody(""""5"""")
            GET("/").expectBody(""""5"""")
        }
    }

    @Test
    fun `supports a optional string query parameter with default`() {
        val param by optionalQuery(
            default = "5",
            emptyAsMissing = true,
            invalidAsMissing = true,
        )

        given {
            GET() withQuery param isHandledBy { (request[param]).ok }
        } then {
            GET("/?param=4")
                .expectBody(""""4"""")
            GET("/?param=")
                .expectBody(""""5"""")
            GET("/").expectBody(""""5"""")
        }
    }

    @Test
    fun `supports a optional string query parameter without default`() {
        val param by optionalQuery(
            emptyAsMissing = true,
        )

        given {
            GET() withQuery param isHandledBy { (request[param] ?: "42").ok }
        } then {
            GET("/?param=4")
                .expectBody(""""4"""")
            GET("/?param=")
                .expectBody(""""42"""")
            GET("/").expectBody(""""42"""")
        }
    }
}