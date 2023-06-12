package undertow.snitch

import com.google.gson.annotations.SerializedName
import com.snitch.me.snitchon.NonEmptyString
import com.snitch.me.snitchon.NonNegativeInt
import com.snitch.me.snitchon.Validator
import me.snitchon.*
import me.snitchon.types.ContentType
import me.snitchon.documentation.Description
import me.snitchon.documentation.Visibility
import me.snitchon.documentation.Visibility.*
import me.snitchon.parameters.PathParam
import me.snitchon.parameters.header
import me.snitchon.parameters.optionalQuery
import me.snitchon.parameters.path
import me.snitchon.parsing.Parser
import me.snitchon.request.Handler
import me.snitchon.types.Format
import me.snitchon.types.Sealed
import java.util.*

val root = "home"
val v1 = "/v1"
val clips = "clips"

val clipId: PathParam<Int, Int> = path(
    name = "clipId",
    condition = NonNegativeInt,
    description = "The clip id"
)

val otherPathParam: PathParam<Int, Int> = path(
    name = "otherPathParam",
    condition = NonNegativeInt,
    description = "The clip id"
)

val name = header(
    name = "clips",
    condition = NonEmptyString,
    description = "The clip id"
)

private val query = optionalQuery(
    name = "query",
    default = "978",
    description = "The query",
    condition = NonEmptyString
)

private val length = optionalQuery(
    name = "length",
    description = "The number of items returned in the page",
    default = 20,
    condition = NonNegativeInt,
    visibility = INTERNAL
)

private val offset = optionalQuery(
    name = "offset",
    description = "The offset from the first item",
    default = 0,
    condition = NonNegativeInt
)

private val random = optionalQuery(
    name = "random",
    invalidAsMissing = true,
    emptyAsMissing = true,
    description = "randomfoo",
    condition = aFoo
)


object aFoo : Validator<Random, Random> {
    override val regex: Regex = "/^hello".toRegex()
    override val description: String = "a ranom parameter"
    override val parse: Parser.(String) -> Random = { Random() }
}

val ServerRouter: Router.() -> Unit = {
//    val getGreeting: Handler<Nothing, AResponse> = {
//        request[query]
//        AResponse(0, 0, 0, listOf(Query("hey")), FooEnum.A).ok
//    }

    POST("bytearray")
        .with(body<ByteArray>(ContentType.APPLICATION_OCTET_STREAM))
        .isHandledBy {
                body.ok.format(Format.OctetStream)
        }

    val getPathGreeting by Handler<Nothing, _,_> {
        request[query]
        AResponse(request[clipId], 0, 0, listOf(Query("hey")), FooEnum.A).created
    }

    val getGreetingBody by Handler<TestClass, _, _> {
        request[query]
        AResponse(0, 0, 0, listOf(Query("hey")), FooEnum.A).accepted
    }

//    "hello" GET "hey" withQuery queries(query) isHandledBy getGreeting

//    http GET "json" isHandledBy { Response("Hello world").ok }

    GET(v1 / clips / clipId) with
            Visibility.INTERNAL with
            queries(random, query, length, offset, query) isHandledBy
            getPathGreeting

    POST(v1 / clips / clipId) with queries(
        random,
        query,
        length,
        offset
    ) with body<TestClass>() with headers(name) isHandledBy getGreetingBody

//        "Run a cute test 2" POST
//                v1 / clips / clipId withQuery queries(query, length, offset) withQuery headers(name) withQuery body<RequestBody>() isHandledBy getGreetingBody

    PUT(v1 / clips / clipId)
        .with(queries(query, length, offset))
        .with(headers(name))
        .copy(tags = listOf("sisi"))
        .isHandledBy(getPathGreeting)

    GET(v1 / "hello").copy(tags = listOf("nono")).isHandledBy { "hello".ok }

    GET(v1 / "nonosisi").copy(tags = listOf("nono", "sisi")).isHandledBy { "".ok }
    GET(v1 / "mama").copy(tags = listOf("ma", "tata")).isHandledBy { "".ok }
    DELETE(v1 / clips / clipId).with(queries(query, length, offset)).with(headers(name)) isHandledBy getPathGreeting

    "haha section" {
        GET(v1 / "haha").isHandledBy { "".ok }
    }
}

enum class FooEnum {
    A, B, C, D
}

data class Response(val message: String)

data class Query(val value: String)
data class AResponse(
    val clipId: Int, val length: Int,
    val offset: Int, val queries: List<Query>?, val enum: FooEnum
)

data class RequestBody(val hello: String)

data class TestClass(
    @Description("no way punk", exString = "https://google.com")
    val aString: String,
    @Description("The best int", exInt = 33)
    @SerializedName("an_intt")
    val aInt: Int,
    val aLong: Long,
    val aFloat: Float,
    val aDouble: Double,
    val aBoolean: Boolean,
    val aBinary: ByteArray,
    val aDate: Date,
    val aListString: List<String>,
    @Description("A great list", exEmptyList = true)
    val aListBoolean: List<Boolean>,
    val anObjectList: List<SimpleObject>,
    @Description(visibility = INTERNAL)
    val theEnum: ClassicEnum,
    val aSeal: MySeal,
    val moreSeals: List<MySeal>
)

data class SimpleObject(
    val a: String,
    val b: Int,
    val c: Long
)

enum class ClassicEnum {
    foo, bar, baz
}

sealed class MySeal : Sealed() {
    data class Bar(val x: String) : MySeal()
    data class Yo(val y: Int) : MySeal()
    object nope : MySeal()
}
