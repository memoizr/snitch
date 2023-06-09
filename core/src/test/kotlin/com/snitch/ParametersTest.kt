package com.snitch

import me.snitchon.documentation.Visibility
import com.snitch.me.snitchon.NonEmptySingleLineString
import com.snitch.me.snitchon.NonEmptyString
import com.snitch.me.snitchon.NonNegativeInt
import com.snitch.me.snitchon.Validator
import me.snitchon.parameters.*
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.parsers.GsonJsonParser.serialized
import me.snitchon.request.Handler
import me.snitchon.request.body
import me.snitchon.request.headers
import me.snitchon.request.queries
import me.snitchon.response.ErrorHttpResponse
import me.snitchon.types.Sealed
import me.snitchon.response.ok
import me.snitchon.types.ErrorResponse
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

val stringParam = path(
    name = "stringParam",
    description = "Description",
    condition = NonEmptySingleLineString
)

val intparam = path(
    name = "intParam",
    description = "Description",
    condition = NonNegativeInt
)

val q = query(name = "q", description = "description", condition = NonEmptyString)
val int = query(name = "int", description = "description", condition = NonNegativeInt, emptyAsMissing = true)
private val offset =
    optionalQuery(name = "offset", description = "description", condition = NonNegativeInt, default = 30)
val limit = optionalQuery(name = "limit", description = "description", condition = NonNegativeInt)

val qHead = header(name = "q", description = "description", condition = NonEmptySingleLineString)
val intHead = header(name = "int", description = "description", condition = NonNegativeInt)
val offsetHead = optionalHeader(
    name = "offsetHead",
    description = "description",
    condition = NonNegativeInt,
    default = 666,
    emptyAsMissing = true
)
val limitHead =
    optionalHeader(name = "limitHead", description = "description", condition = NonNegativeInt, emptyAsMissing = true)
val queryParam =
    optionalQuery(name = "param", description = "parameter", condition = NonEmptySingleLineString, default = "hey")
val headerParam = optionalHeader(
    name = "param",
    description = "parameter",
    condition = NonEmptySingleLineString,
    default = "hey",
    visibility = Visibility.INTERNAL
)
val pathParam = path(name = "param", description = "parameter", condition = NonEmptySingleLineString)

val time = query("time", description = "the time", condition = DateValidator)

object DateValidator : Validator<Date, Date> {
    override val description: String = "An iso 8601 format date"
    override val regex: Regex =
        """^(?:[1-9]\d{3}-(?:(?:0[1-9]|1[0-2])-(?:0[1-9]|1\d|2[0-8])|(?:0[13-9]|1[0-2])-(?:29|30)|(?:0[13578]|1[02])-31)|(?:[1-9]\d(?:0[48]|[2468][048]|[13579][26])|(?:[2468][048]|[13579][26])00)-02-29)T(?:[01]\d|2[0-3]):[0-5]\d:[0-5]\d(?:Z|[+-][01]\d:[0-5]\d)$""".toRegex()
    override val parse: (String) -> Date = { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").parse(it) }
}

class ParametersTest : BaseTest(routes {
    GET("stringpath" / stringParam) isHandledBy { TestResult(request[stringParam]).ok }
    GET("intpath" / intparam) isHandledBy { IntTestResult(request[intparam]).ok }

    GET("intpath2" / intparam / "end") isHandledBy {
        IntTestResult(request[intparam]).ok
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
        whenPerform GET "/$root/stringpath/hellothere" expectBodyJson TestResult("hellothere")
        whenPerform GET "/$root/intpath/300" expectBodyJson IntTestResult(300)
    }

    @Test
    fun `validates path parameters`() {
        whenPerform GET "/$root/intpath2/4545/end" expectBody IntTestResult(4545).serialized
        whenPerform GET "/$root/intpath2/hello/end" expectBody ErrorResponse(
            400,
            listOf("Path parameter `intParam` is invalid, expecting non negative integer, got `hello`")
        ).serialized
    }

    @Test
    fun `supports query parameters`() {
        whenPerform GET "/$root/queriespath?q=foo" expectBodyJson TestResult("foo")
        whenPerform GET "/$root/queriespath?q=foo%0Abar" expectBodyJson TestResult("foo\nbar")
        whenPerform GET "/$root/queriespath?q=" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Query parameter `q` is invalid, expecting non empty string, got ``")
        )
        whenPerform GET "/$root/queriespath" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Required Query parameter `q` is missing")
        )

        whenPerform GET "/$root/queriespath2?int=3434" expectBodyJson IntTestResult(3434)
        whenPerform GET "/$root/queriespath2?int=" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Required Query parameter `int` is missing")
        )
        whenPerform GET "/$root/queriespath2?int=hello" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Query parameter `int` is invalid, expecting non negative integer, got `hello`")
        )
        whenPerform GET "/$root/queriespath2?int=-34" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Query parameter `int` is invalid, expecting non negative integer, got `-34`")
        )
    }

    @Test
    fun `supports default values for query parameters`() {
        with (GsonJsonParser) {
            whenPerform GET "/$root/queriespath3?offset=42" expectBody IntTestResult(42).serialized
            whenPerform GET "/$root/queriespath3" expectBody IntTestResult(30).serialized

            whenPerform GET "/$root/queriespath4?limit=42" expectBody NullableIntTestResult(42).serialized
            whenPerform GET "/$root/queriespath4" expectBody """{}"""
        }
    }

    @Test
    fun `supports header parameters`() {
        whenPerform GET "/$root/headerspath" withHeaders mapOf(qHead.name to "foo") expectBodyJson TestResult("foo")
        whenPerform GET "/$root/headerspath" withHeaders mapOf(qHead.name to "") expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Header parameter `q` is invalid, expecting non empty single-line string, got ``")
        )
        whenPerform GET "/$root/headerspath" withHeaders mapOf() expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Required Header parameter `q` is missing")
        )

        whenPerform GET "/$root/headerspath2" withHeaders mapOf(intHead.name to 3434) expectBodyJson IntTestResult(3434)
        whenPerform GET "/$root/headerspath2" expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Required Header parameter `int` is missing")
        )
        whenPerform GET "/$root/headerspath2" withHeaders mapOf(intHead.name to "hello") expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Header parameter `int` is invalid, expecting non negative integer, got `hello`")
        )
        whenPerform GET "/$root/headerspath2" withHeaders mapOf(intHead.name to -34) expectBodyJson TestErrorHttpResponse<TestResult, List<String>>(
            400,
            listOf("Header parameter `int` is invalid, expecting non negative integer, got `-34`")
        )
    }

    @Test
    fun `supports default values for header parameters`() {
        with (GsonJsonParser) {
            whenPerform GET "/$root/headerspath3" withHeaders mapOf(offsetHead.name to 42) expectBody IntTestResult(42).serialized
            whenPerform GET "/$root/headerspath3" expectBody IntTestResult(666).serialized
            whenPerform GET "/$root/headerspath3" expectBody IntTestResult(666).serialized

            whenPerform GET "/$root/headerspath4" withHeaders mapOf(limitHead.name to 42) expectBody NullableIntTestResult(
                42
            ).serialized
            whenPerform GET "/$root/headerspath4" withHeaders mapOf(limitHead.name to "") expectBody """{}"""
            whenPerform GET "/$root/headerspath4" expectBody """{}"""
        }
    }

    val bodyParam = BodyParam(42, "hello", SealedClass.One(33))

    @Test
    fun `supports body parameter`() {
        whenPerform POST "/$root/bodyparam" withBody bodyParam expectBody BodyTestResult(42, 33).serialized
    }

    @Test
    fun `supports custom parsing`() {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        val date = "2018-06-30T02:59:51-00:00"
        with (GsonJsonParser) {
            whenPerform GET "/$root/customParsing?time=$date" expectBody DateResult(df.parse(date)).serialized
        }
    }

    @Test
    fun `forbids using parameters which aren't registered`() {
        with (GsonJsonParser) {
            whenPerform GET "/$root/sneakyqueryparams" expectBody ErrorResponse(
                500,
                "Attempting to use unregistered query parameter `param`"
            ).serialized
            whenPerform GET "/$root/sneakyheaderparams" expectBody ErrorResponse(
                500,
                "Attempting to use unregistered header parameter `param`"
            ).serialized
            whenPerform GET "/$root/sneakypathparams/343" expectBody ErrorResponse(
                500,
                "Attempting to use unregistered path parameter `param`"
            ).serialized
        }
    }

    @Test
    fun `returns error for failed parsing of body parameter`() {
        whenPerform POST "/$root/bodyparam" withBody "lolol" expectCode 400 expectBody """{"statusCode":400,"details":"Invalid body parameter"}"""
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
