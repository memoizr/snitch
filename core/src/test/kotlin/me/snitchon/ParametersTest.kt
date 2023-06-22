package me.snitchon

import me.snitchon.documentation.Visibility
import com.snitch.me.snitchon.NonEmptySingleLineString
import com.snitch.me.snitchon.NonEmptyString
import com.snitch.me.snitchon.NonNegativeInt
import com.snitch.me.snitchon.Validator
import me.snitchon.parameters.*
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.parsers.GsonJsonParser.serialized
import me.snitchon.parsing.Parser
import me.snitchon.request.Handler
import me.snitchon.types.Sealed
import me.snitchon.types.ErrorResponse
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

val stringParam by path(
    description = "Description",
    condition = NonEmptySingleLineString
)

val intParam by path(
    description = "Description",
    condition = NonNegativeInt
)

val q by query()
val int by query(condition = NonNegativeInt, emptyAsMissing = true)
private val offset by
    optionalQuery(condition = NonNegativeInt, description = "description", default = 30)
val limit by optionalQuery(condition = NonNegativeInt, description = "description")

val qHead by header(condition = NonEmptySingleLineString, name = "q", description = "description")
val intHead by header(condition = NonNegativeInt, name = "int", description = "description")
val offsetHead by optionalHeader(
    condition = NonNegativeInt,
    description = "description",
    emptyAsMissing = true,
    default = 666
)
val limitHead by
    optionalHeader(condition = NonNegativeInt, description = "description", emptyAsMissing = true)
val queryParam by
    optionalQuery(condition = NonEmptySingleLineString, name = "param", description = "parameter", default = "hey")
val headerParam by optionalHeader(
    condition = NonEmptySingleLineString,
    name = "param",
    description = "parameter",
    default = "hey",
    visibility = Visibility.INTERNAL
)
val pathParam by path(name = "param", description = "parameter", condition = NonEmptySingleLineString)

val time by query(condition = DateValidator, "time", description = "the time")

object DateValidator : Validator<Date, Date> {
    override val description: String = "An iso 8601 format date"
    override val regex: Regex =
        """^(?:[1-9]\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\d|2[0-3]):[0-5]\d:[0-5]\d(?:Z|[+-][01]\d:[0-5]\d)$""".toRegex()
    override val parse: Parser.(String) -> Date = { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(it) }
}

class ParametersTest : BaseTest(testRoutes {
    GET("stringpath" / stringParam) isHandledBy { TestResult(request[stringParam]).ok }
    GET("intpath" / intParam) isHandledBy { IntTestResult(request[intParam]).ok }

    GET("intpath2" / intParam / "end") isHandledBy {
        IntTestResult(request[intParam]).ok
    }

    GET("queriespath") inSummary "does a foo" withQuery q isHandledBy { TestResult(request[q]).ok }

    GET("queriespath2") with queries(int) isHandledBy { IntTestResult(request[int]).ok }
    GET("queriespath3") with queries(offset) isHandledBy { IntTestResult(request[offset]).ok }
    GET("queriespath4") with queries(limit) isHandledBy { NullableIntTestResult(request[limit]).ok }

    GET("headerspath") with headers(qHead) isHandledBy { TestResult(request[qHead]).ok }
    GET("headerspath2") with headers(intHead) isHandledBy { IntTestResult(request[intHead]).ok }
    GET("headerspath3") with headers(offsetHead) isHandledBy { NullableIntTestResult(request[offsetHead]).ok }
    GET("headerspath4") with headers(limitHead) isHandledBy { NullableIntTestResult(request[limitHead]).ok }

    GET("customParsing") with queries(time) isHandledBy { DateResult(request[time]).ok }

    GET("sneakyqueryparams") isHandledBy { TestResult(request.get(queryParam)).ok }
    GET("sneakyheaderparams") isHandledBy { TestResult(request.get(headerParam)).ok }
    GET("sneakypathparams" / pathParam.copy(name = "sneaky")) isHandledBy { TestResult(request.get(pathParam)).ok }

    val function by Handler<BodyParam, _, _> {
        val sealed = body.sealed
        BodyTestResult(
            body.int, when (sealed) {
                is SealedClass.One -> sealed.oneInt
                is SealedClass.Two -> 2
            }
        ).ok
    }
    POST("bodyparam") with body<BodyParam>() isHandledBy function
}) {

    @Test
    fun `supports typed path parameters`() {
        whenPerform GET "/stringpath/hellothere" expectBodyJson TestResult("hellothere")
        whenPerform GET "/intpath/300" expectBodyJson IntTestResult(300)
    }

    @Test
    fun `validates path parameters`() {
        whenPerform GET "/intpath2/4545/end" expectBody IntTestResult(4545).serialized
        whenPerform GET "/intpath2/hello/end" expectBody ErrorResponse(
            400,
            listOf("Path parameter `intParam` is invalid, expecting non negative integer, got `hello`")
        ).serialized
    }

    @Test
    fun `supports query parameters`() {
        whenPerform GET "/queriespath?q=foo" expectBodyJson TestResult("foo")
        whenPerform GET "/queriespath?q=foo%0Abar" expectBodyJson TestResult("foo\nbar")
        whenPerform GET "/queriespath?q=" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Query parameter `q` is invalid, expecting non empty string, got ``")
        )
        whenPerform GET "/queriespath" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Required Query parameter `q` is missing")
        )

        whenPerform GET "/queriespath2?int=3434" expectBodyJson IntTestResult(3434)
        whenPerform GET "/queriespath2?int=" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Required Query parameter `int` is missing")
        )
        whenPerform GET "/queriespath2?int=hello" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Query parameter `int` is invalid, expecting non negative integer, got `hello`")
        )
        whenPerform GET "/queriespath2?int=-34" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Query parameter `int` is invalid, expecting non negative integer, got `-34`")
        )
    }

    @Test
    fun `supports default values for query parameters`() {
        with (GsonJsonParser) {
            whenPerform GET "/queriespath3?offset=42" expectBody IntTestResult(42).serialized
            whenPerform GET "/queriespath3" expectBody IntTestResult(30).serialized

            whenPerform GET "/queriespath4?limit=42" expectBody NullableIntTestResult(42).serialized
            whenPerform GET "/queriespath4" expectBody """{}"""
        }
    }

    @Test
    fun `supports header parameters`() {
        whenPerform GET "/headerspath" withHeaders mapOf(qHead.name to "foo") expectBodyJson TestResult("foo")
        whenPerform GET "/headerspath" withHeaders mapOf(qHead.name to "") expectBody TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Header parameter `q` is invalid, expecting non empty single-line string, got ``")
        ).serialized
        whenPerform GET "/headerspath" withHeaders mapOf() expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Required Header parameter `q` is missing")
        )

        whenPerform GET "/headerspath2" withHeaders mapOf(intHead.name to 3434) expectBodyJson IntTestResult(3434)
        whenPerform GET "/headerspath2" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Required Header parameter `int` is missing")
        )
        whenPerform GET "/headerspath2" withHeaders mapOf(intHead.name to "hello") expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Header parameter `int` is invalid, expecting non negative integer, got `hello`")
        )
        whenPerform GET "/headerspath2" withHeaders mapOf(intHead.name to -34) expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Header parameter `int` is invalid, expecting non negative integer, got `-34`")
        )
    }

    @Test
    fun `supports default values for header parameters`() {
        with (GsonJsonParser) {
            whenPerform GET "/headerspath3" withHeaders mapOf(offsetHead.name to 42) expectBody IntTestResult(42).serialized
            whenPerform GET "/headerspath3" expectBody IntTestResult(666).serialized
            whenPerform GET "/headerspath3" expectBody IntTestResult(666).serialized

            whenPerform GET "/headerspath4" withHeaders mapOf(limitHead.name to 42) expectBody NullableIntTestResult(
                42
            ).serialized
            whenPerform GET "/headerspath4" withHeaders mapOf(limitHead.name to "") expectBody """{}"""
            whenPerform GET "/headerspath4" expectBody """{}"""
        }
    }

    val bodyParam = BodyParam(42, "hello", SealedClass.One(33))

    @Test
    fun `supports body parameter`() {
        whenPerform POST "/bodyparam" withBody bodyParam expectBody BodyTestResult(42, 33).serialized
    }

    @Test
    fun `supports custom parsing`() {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        val date = "2018-06-30T02:59:51-00:00"
        with (GsonJsonParser) {
            whenPerform GET "/customParsing?time=$date" expectBody DateResult(df.parse(date)).serialized
        }
    }

    @Test
    fun `forbids using parameters which aren't registered`() {
        with (GsonJsonParser) {
            whenPerform GET "/sneakyqueryparams" expectBody ErrorResponse(
                500,
                "Attempting to use unregistered query parameter `param`"
            ).serialized
            whenPerform GET "/sneakyheaderparams" expectBody ErrorResponse(
                500,
                "Attempting to use unregistered header parameter `param`"
            ).serialized
            whenPerform GET "/sneakypathparams/343" expectBody ErrorResponse(
                500,
                "Attempting to use unregistered path parameter `param`"
            ).serialized
        }
    }

    @Test
    fun `returns error for failed parsing of body parameter`() {
        whenPerform POST "/bodyparam" withBody "lolol" expectCode 400 expectBody """{"statusCode":400,"details":"Invalid body parameter"}"""
    }

    data class IntTestResult(val result: Int)
    data class NullableIntTestResult(val result: Int?)
    data class DateResult(val date: Date)

    data class BodyParam(val int: Int, val string: String, val sealed: SealedClass)
    data class BodyTestResult(val a: Int, val b: Int)

    sealed class SealedClass : Sealed() {
        data class One(val oneInt: Int) : SealedClass()
        object Two : SealedClass()
    }
}

data class TestErrorHttpResponse<T, E>(
    val statusCode: Int,
    val details: E,
)
