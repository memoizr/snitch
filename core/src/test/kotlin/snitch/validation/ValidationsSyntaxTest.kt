package snitch.validation

import org.junit.jupiter.api.Test
import snitch.dsl.InlineSnitchTest
import snitch.parameters.TestErrorHttpResponse
import snitch.parameters.optionalHeader
import snitch.parameters.optionalQuery
import snitch.parameters.path
import snitch.parsers.GsonJsonParser.serialized
import snitch.types.Parser

// Path parameters
private val id by path(condition = ofNonNegativeInt)

// Query parameters
private val offset by optionalQuery(condition = ofNonNegativeInt, "offset", default = 20, emptyAsMissing = true)
private val allowInvalidQuery by optionalQuery(
    condition = ofNonNegativeInt,
    "allowInvalidQuery",
    default = 20,
    emptyAsMissing = true,
    invalidAsMissing = true
)
private val stringSet by optionalQuery(condition = ofStringSet, "stringset")
private val userId by optionalQuery(condition = UserIdValidator, "userId")

// Header parameters
private val allowInvalidHeader by optionalHeader(
    condition = ofNonNegativeInt,
    "allowInvalidHeader",
    emptyAsMissing = true,
    default = 20,
    invalidAsMissing = true
)

// Custom validator
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
    fun `validates path parameters`() {
        given {
            GET("foo" / id) isHandledBy {
                request[id]
                "ok".ok
            }
        } then {
            GET("/foo/3456").expectCode(200).expectBody(""""ok"""")
            
            GET("/foo/hey").expectBody(
                TestErrorHttpResponse<Any, List<String>>(
                    400,
                    listOf("""Path parameter `id` is invalid, expecting non negative integer, got `hey`""")
                ).serialized
            ).expectCode(400)
        }
    }
    
    @Test
    fun `validates query parameters`() {
        given {
            GET("query-validation" / id) with queries(offset) isHandledBy {
                request[offset]
                request[id]
                "ok".ok
            }
        } then {
            GET("/query-validation/123").expectCode(200).expectBody(""""ok"""")
            GET("/query-validation/123?offset=30").expectCode(200).expectBody(""""ok"""")
            
            GET("/query-validation/123?offset=-34").expectBody(
                TestErrorHttpResponse<Any, List<String>>(
                    400,
                    listOf("""Query parameter `offset` is invalid, expecting non negative integer, got `-34`""")
                ).serialized
            ).expectCode(400)
            
            GET("/query-validation/123?offset=a").expectBody(
                TestErrorHttpResponse<Any, List<String>>(
                    400,
                    listOf("""Query parameter `offset` is invalid, expecting non negative integer, got `a`""")
                ).serialized
            ).expectCode(400)
        }
    }

    @Test
    fun `supports invalidAsMissing for query parameters`() {
        given {
            GET("invalid-query" / id) with queries(allowInvalidQuery) isHandledBy {
                request[allowInvalidQuery]
                request[id]
                "ok".ok
            }
        } then {
            GET("/invalid-query/123").expectCode(200).expectBody(""""ok"""")
            GET("/invalid-query/123?allowInvalidQuery=42").expectCode(200).expectBody(""""ok"""")
            GET("/invalid-query/123?allowInvalidQuery=a").expectCode(200).expectBody(""""ok"""")
        }
    }
    
    @Test
    fun `supports invalidAsMissing for header parameters`() {
        given {
            GET("invalid-header" / id) with headers(allowInvalidHeader) isHandledBy {
                request[allowInvalidHeader]
                request[id]
                "ok".ok
            }
        } then {
            GET("/invalid-header/123").expectCode(200).expectBody(""""ok"""")
            GET("/invalid-header/123").withHeaders(mapOf(allowInvalidHeader.name to "42")).expectCode(200).expectBody(""""ok"""")
            GET("/invalid-header/123").withHeaders(mapOf(allowInvalidHeader.name to "boo")).expectCode(200).expectBody(""""ok"""")
        }
    }
    
    @Test
    fun `supports string set validation`() {
        given {
            GET("string-set" / id) with queries(stringSet) isHandledBy {
                request[stringSet]
                request[id]
                "ok".ok
            }
        } then {
            GET("/string-set/11?stringset=foo").expectBody(""""ok"""")
            GET("/string-set/11?stringset=foo%20bar").expectBody(""""ok"""")
            GET("/string-set/11?stringset=foo,bar").expectBody(""""ok"""")
            
            // Via header (even though it's defined as a query param)
            GET("/string-set/11").withHeaders(mapOf(stringSet.name to "foo")).expectBody(""""ok"""")
            GET("/string-set/11").withHeaders(mapOf(stringSet.name to "foo,bar")).expectBody(""""ok"""")
        }
    }
    
    @Test
    fun `supports custom validators`() {
        given {
            GET("custom-validator" / id) with queries(userId) isHandledBy {
                request[userId]
                request[id]
                "ok".ok
            }
        } then {
            GET("/custom-validator/11?userId=nooooo").expectBody(""""ok"""")
        }
    }
    
    @Test
    fun `validates complex route with multiple parameters`() {
        given {
            GET("complex" / id) with queries(
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
            // Test successful request with valid parameters
            GET("/complex/3456")
                .withHeaders(mapOf(allowInvalidHeader.name to "30"))
                .expectCode(200)
                .expectBody(""""ok"""")
            
            // Test with invalid query that's allowed to be invalid
            GET("/complex/3456?allowInvalidQuery=invalid")
                .expectCode(200)
                .expectBody(""""ok"""")
                
            // Test with invalid path parameter
            GET("/complex/invalid")
                .expectBody(
                    TestErrorHttpResponse<Any, List<String>>(
                        400,
                        listOf("""Path parameter `id` is invalid, expecting non negative integer, got `invalid`""")
                    ).serialized
                )
                .expectCode(400)
        }
    }
}
