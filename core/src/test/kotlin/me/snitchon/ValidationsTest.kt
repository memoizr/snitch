package me.snitchon

import com.snitch.me.snitchon.NonNegativeInt
import com.snitch.me.snitchon.StringSet
import com.snitch.me.snitchon.Validator
import me.snitchon.parameters.*
import me.snitchon.parsers.GsonJsonParser.serialized
import me.snitchon.parsing.Parser
import org.junit.Test

private val offset by optionalQuery(condition = NonNegativeInt, "offset", default = 20, emptyAsMissing = true)
private val id by path(condition = NonNegativeInt, description = "")
private val allowInvalidQuery by optionalQuery(
    condition = NonNegativeInt,
    "allowInvalidQuery",
    default = 20,
    emptyAsMissing = true,
    invalidAsMissing = true
)
private val allowInvalidHeader by optionalHeader(
    condition = NonNegativeInt,
    "allowInvalidHeader",
    emptyAsMissing = true,
    default = 20,
    invalidAsMissing = true
)
private val stringSet by optionalQuery(condition = StringSet, "stringset")
private val userId by optionalQuery(condition = UserIdValidator, "userId")

private data class UserId(val id: String)

private object UserIdValidator : Validator<String, UserId> {
    override val description = "User id"
    override val regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL)
    override val parse: Parser.(String) -> UserId = { UserId(it) }
}

class ValidationsTest : BaseTest(testRoutes {
    GET("foo" / id) with queries(
        offset,
        allowInvalidQuery,
        stringSet,
        userId
    ) with headers(allowInvalidHeader) isHandledBy {
        request[offset]
        request[allowInvalidHeader]
        request[allowInvalidQuery]
        request[stringSet]
        request[userId]
        request[id]
        "ok".ok
    }
}) {

    @Test
    fun `validates routes`() {
        whenPerform GET "/foo/3456" expectCode 200
        whenPerform GET "/foo/hey" expectBody TestErrorHttpResponse<Any, List<String>>(
            400,
            listOf("""Path parameter `id` is invalid, expecting non negative integer, got `hey`""")
        ).serialized expectCode 400
        whenPerform GET "/foo/134?offset=-34" expectBodyJson TestErrorHttpResponse<Any, List<String>>(
            400,
            listOf("""Query parameter `offset` is invalid, expecting non negative integer, got `-34`""")
        ) expectCode 400
        whenPerform GET "/foo/134?offset=a" expectBodyJson TestErrorHttpResponse<Any, List<String>>(
            400,
            listOf("""Query parameter `offset` is invalid, expecting non negative integer, got `a`""")
        ) expectCode 400
        whenPerform GET "/foo/134?allowInvalidQuery=a" expectCode 200
        whenPerform GET "/foo/134" withHeaders mapOf(allowInvalidHeader.name to "boo") expectCode 200
        whenPerform GET "/foo/134" withHeaders mapOf(allowInvalidHeader.name to "boo") expectCode 200

        whenPerform GET "/foo/11" withHeaders mapOf(stringSet.name to "foo") expectBody """"ok""""
        whenPerform GET "/foo/11" withHeaders mapOf(stringSet.name to "foo,bar") expectBody """"ok""""
        whenPerform GET "/foo/11?stringset=foo" expectBody """"ok""""
        whenPerform GET "/foo/11?stringset=foo%20bar" expectBody """"ok""""
        whenPerform GET "/foo/11?stringset=foo,bar" expectBody """"ok""""
        whenPerform GET "/foo/11?userId=nooooo" expectBody """"ok""""
    }
}
