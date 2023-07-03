package snitch.validation

import org.junit.Test
import snitch.dsl.InlineSnitchTest
import snitch.parameters.TestErrorHttpResponse
import snitch.parameters.optionalHeader
import snitch.parameters.optionalQuery
import snitch.parameters.path
import snitch.parsers.GsonJsonParser.serialized
import snitch.parsing.Parser

private val offset by optionalQuery(condition = ofNonNegativeInt, "offset", default = 20, emptyAsMissing = true)
private val id by path(condition = ofNonNegativeInt)
private val allowInvalidQuery by optionalQuery(
    condition = ofNonNegativeInt,
    "allowInvalidQuery",
    default = 20,
    emptyAsMissing = true,
    invalidAsMissing = true
)
private val allowInvalidHeader by optionalHeader(
    condition = ofNonNegativeInt,
    "allowInvalidHeader",
    emptyAsMissing = true,
    default = 20,
    invalidAsMissing = true
)
private val stringSet by optionalQuery(condition = ofStringSet, "stringset")
private val userId by optionalQuery(condition = UserIdValidator, "userId")

private data class UserId(val id: String)

private object UserIdValidator : Validator<String, UserId> {
    override val description = "User id"
    override val regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL)
    override val parse: Parser.(Collection<String>) -> UserId = {
        it.first().let {
            UserId(it)
        }
    }
}

class ValidationsSyntaxTest : InlineSnitchTest() {

    @Test
    fun `validates routes`() {
        given {
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
        } then {
            GET("/foo/3456").expectCode(200)
            GET("/foo/hey").expectBody(
                TestErrorHttpResponse<Any, List<String>>(
                    400,
                    listOf("""Path parameter `id` is invalid, expecting non negative integer, got `hey`""")
                ).serialized
            ).expectCode(400)
            GET("/foo/134?offset=-34").expectBody(
                TestErrorHttpResponse<Any, List<String>>(
                    400,
                    listOf("""Query parameter `offset` is invalid, expecting non negative integer, got `-34`""")
                ).serialized
            ).expectCode(400)
            GET("/foo/134?offset=a").expectBody(
                TestErrorHttpResponse<Any, List<String>>(
                    400,
                    listOf("""Query parameter `offset` is invalid, expecting non negative integer, got `a`""")
                ).serialized
            ).expectCode(400)
            GET("/foo/134?allowInvalidQuery=a").expectCode(200)
            GET("/foo/134").withHeaders(mapOf(allowInvalidHeader.name to "boo")).expectCode(200)
            GET("/foo/134").withHeaders(mapOf(allowInvalidHeader.name to "boo")).expectCode(200)

            GET("/foo/11").withHeaders(mapOf(stringSet.name to "foo")).expectBody(""""ok"""")
            GET("/foo/11").withHeaders(mapOf(stringSet.name to "foo,bar")).expectBody(""""ok"""")
            GET("/foo/11?stringset=foo").expectBody(""""ok"""")
            GET("/foo/11?stringset=foo%20bar").expectBody(""""ok"""")
            GET("/foo/11?stringset=foo,bar").expectBody(""""ok"""")
            GET("/foo/11?userId=nooooo").expectBody(""""ok"""")
        }
    }
}
