package undertow.snitch

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.memoizr.assertk.expect
import me.snitchon.*
import me.snitchon.types.ContentType
import me.snitchon.documentation.generateDocumentation
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser.serialized
import me.snitchon.parsers.GsonJsonParser.parse
import me.snitchon.request.Handler
import me.snitchon.tests.SnitchTest
import me.snitchon.types.Format
import org.junit.Test

private val listHandler by Handler<Nothing, _, _> {
    listOf(SampleClass("hey", listOf())).ok
}

private val createdHandler by Handler<Nothing, _, _> {
    listOf(SampleClass("hey", listOf())).created
}

private val genericHandler by Handler<Nothing,_, _> {
    GenericResponse(GenericResponse(Foo1("hey"))).ok
}

class DocumentationTest : SnitchTest(routes {
    GET("one")
        .isHandledBy { SampleClass("hey", listOf()).ok }
    GET("two")
        .isHandledBy(listHandler)
    GET("generic")
        .isHandledBy(genericHandler)
    GET("created")
        .isHandledBy(createdHandler)
    POST("bytearray")
        .with(body<ByteArray>(ContentType.APPLICATION_OCTET_STREAM))
        .isHandledBy { body.ok.format(Format.OctetStream) }
}) {

    @Test
    fun `uses custom serialization`() {
        val docs = activeService.generateDocumentation(GsonDocumentationSerializer).documentation.spec

        expect that docs.serialized contains "a_sample"
    }

    @Test
    fun `supports generic response types`() {
        val docs = activeService.generateDocumentation(GsonDocumentationSerializer).documentation.spec

        expect that docs.serialized contains "foo"
    }

    @Test
    fun `supports binary request types`() {
        val docs = activeService.generateDocumentation(GsonDocumentationSerializer).documentation.spec.parse(JsonObject::class.java)
            .getAsJsonObject("paths")
            .getAsJsonObject("/bytearray")
            .getAsJsonObject("post")
            .getAsJsonObject("requestBody")
            .getAsJsonObject("content")

        expect that docs.serialized contains """"format":"byte"""" contains "octet-stream"
    }

    @Test
    fun `supports binary response types`() {
        val docs = activeService.generateDocumentation(GsonDocumentationSerializer).documentation.spec.parse(JsonObject::class.java)
            .getAsJsonObject("paths")
            .getAsJsonObject("/bytearray")
            .getAsJsonObject("post")
            .getAsJsonObject("responses")
            .getAsJsonObject("200")
            .getAsJsonObject("content")

        expect that docs.serialized contains """"format":"byte"""" contains "octet-stream"
    }

    @Test
    fun `supports response status codes`() {
        val docs = activeService.generateDocumentation(GsonDocumentationSerializer).documentation.spec.parse(JsonObject::class.java)
            .getAsJsonObject("paths")
            .getAsJsonObject("/created")
            .getAsJsonObject("get")
            .getAsJsonObject("responses")

        expect that docs.serialized contains "201"
    }

}

private data class Foo1(val foo: String)
private data class GenericResponse<out T>(val data: T)
private data class SampleClass(
    @SerializedName("a_sample")
    val aString: String,
    val someStrings: List<String>
)
