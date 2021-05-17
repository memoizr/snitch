package com.snitch

import org.junit.Rule
import org.junit.Test

class ValidationsTest : SparkTest() {

    val id = path("id", condition= NonNegativeInt)
    val offset = optionalQuery("offset", condition = NonNegativeInt, default = 20, emptyAsMissing = true)
    val allowInvalidQuery = optionalQuery("allowInvalidQuery",  condition = NonNegativeInt, default = 20, emptyAsMissing = true, invalidAsMissing = true)
    val allowInvalidHeader = optionalHeader("allowInvalidHeader", condition = NonNegativeInt, default = 20, emptyAsMissing = true, invalidAsMissing = true)
    val stringSet = optionalQuery("stringset", condition = StringSet)
    val userId = optionalQuery("userId", condition = UserIdValidator)

    data class UserId(val id: String)

    object UserIdValidator : Validator<String, UserId> {
        override val description = "User id"
        override val regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL)
        override val parse: (String) -> UserId = { UserId(it) }
    }

    @Rule
    @JvmField
    val rule = SparkTestRule(port) {
        GET("foo" / id) with queries(offset, allowInvalidQuery, stringSet, userId) with headers(allowInvalidHeader) isHandledBy {
            request[offset]
            request[allowInvalidHeader]
            request[allowInvalidQuery]
            request[stringSet]
            request[userId]
            "ok".ok
        }
    }

    @Test
    fun `validates routes`() {
        whenPerform GET "/$root/foo/3456" expectCode 200
        whenPerform GET "/$root/foo/hey" expectBodyJson HttpResponse.ErrorHttpResponse<Any, List<String>>(400, listOf("""Path parameter `id` is invalid, expecting non negative integer, got `hey`""")) expectCode 400
        whenPerform GET "/$root/foo/134?offset=-34" expectBodyJson HttpResponse.ErrorHttpResponse<Any, List<String>>(400, listOf("""Query parameter `offset` is invalid, expecting non negative integer, got `-34`""")) expectCode 400
        whenPerform GET "/$root/foo/134?offset=a" expectBodyJson HttpResponse.ErrorHttpResponse<Any, List<String>>(400, listOf("""Query parameter `offset` is invalid, expecting non negative integer, got `a`""")) expectCode 400
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
