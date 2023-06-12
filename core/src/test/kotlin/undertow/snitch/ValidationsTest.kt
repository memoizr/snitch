package undertow.snitch

import com.snitch.me.snitchon.NonNegativeInt
import com.snitch.me.snitchon.StringSet
import com.snitch.me.snitchon.Validator
import me.snitchon.parameters.optionalHeader
import me.snitchon.parameters.optionalQuery
import me.snitchon.parameters.path
import me.snitchon.parsers.GsonJsonParser.serialized
import me.snitchon.parsing.Parser
import org.junit.Test

private val id = path("id", condition = NonNegativeInt)
private val offset = optionalQuery("offset", condition = NonNegativeInt, default = 20, emptyAsMissing = true)
private val allowInvalidQuery = optionalQuery(
    "allowInvalidQuery",
    condition = NonNegativeInt,
    default = 20,
    emptyAsMissing = true,
    invalidAsMissing = true
)
private val allowInvalidHeader = optionalHeader(
    "allowInvalidHeader",
    condition = NonNegativeInt,
    default = 20,
    emptyAsMissing = true,
    invalidAsMissing = true
)
private val stringSet = optionalQuery("stringset", condition = StringSet)
private val userId = optionalQuery("userId", condition = UserIdValidator)

private data class UserId(val id: String)

private object UserIdValidator : Validator<String, UserId> {
    override val description = "User id"
    override val regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL)
    override val parse: Parser.(String) -> UserId = { UserId(it) }
}

class ValidationsTest : BaseTest(routes {
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
        whenPerform GET "/$root/foo/3456" expectCode 200
        whenPerform GET "/$root/foo/hey" expectBody TestErrorHttpResponse<Any, List<String>>(
            400,
            listOf("""Path parameter `id` is invalid, expecting non negative integer, got `hey`""")
        ).serialized expectCode 400
        whenPerform GET "/$root/foo/134?offset=-34" expectBodyJson TestErrorHttpResponse<Any, List<String>>(
            400,
            listOf("""Query parameter `offset` is invalid, expecting non negative integer, got `-34`""")
        ) expectCode 400
        whenPerform GET "/$root/foo/134?offset=a" expectBodyJson TestErrorHttpResponse<Any, List<String>>(
            400,
            listOf("""Query parameter `offset` is invalid, expecting non negative integer, got `a`""")
        ) expectCode 400
        whenPerform GET "/$root/foo/134?allowInvalidQuery=a" expectCode 200
        whenPerform GET "/$root/foo/134" withHeaders mapOf(allowInvalidHeader.name to "boo") expectCode 200
        whenPerform GET "/$root/foo/134" withHeaders mapOf(allowInvalidHeader.name to "boo") expectCode 200

        whenPerform GET "/$root/foo/11" withHeaders mapOf(stringSet.name to "foo") expectBody """"ok""""
        whenPerform GET "/$root/foo/11" withHeaders mapOf(stringSet.name to "foo,bar") expectBody """"ok""""
        whenPerform GET "/$root/foo/11?stringset=foo" expectBody """"ok""""
        whenPerform GET "/$root/foo/11?stringset=foo%20bar" expectBody """"ok""""
        whenPerform GET "/$root/foo/11?stringset=foo,bar" expectBody """"ok""""
        whenPerform GET "/$root/foo/11?userId=nooooo" expectBody """"ok""""
    }
}
