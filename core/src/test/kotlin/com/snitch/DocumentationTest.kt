package com.snitch

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import com.memoizr.assertk.expect
import me.snitchon.*
import me.snitchon.documentation.ContentType
import me.snitchon.documentation.generateDocs
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser.jsonString
import me.snitchon.parsers.GsonJsonParser.parseJson
import me.snitchon.tests.SnitchTest
import org.junit.Test

private val listHandler by Handler<Nothing, _> {
    listOf(SampleClass("hey", listOf())).ok()
}

private val genericHandler by Handler<Nothing, _> {
    GenericResponse(GenericResponse(Foo1("hey"))).ok
}

class DocumentationTest : SnitchTest(routes {
    GET("one")
        .isHandledBy { SampleClass("hey", listOf()).ok() }
    GET("two")
        .isHandledBy(listHandler)
    GET("generic")
        .isHandledBy(genericHandler)
    POST("bytearray")
        .with(body<ByteArray>(ContentType.APPLICATION_OCTET_STREAM))
        .isHandledBy { body.ok.format(Format.OctetStream) }
}) {

    @Test
    fun `uses custom serialization`() {
        val docs = activeService.generateDocs(GsonDocumentationSerializer).spec

        expect that docs.jsonString contains "a_sample"
    }

    @Test
    fun `supports generic response types`() {
        val docs = activeService.generateDocs(GsonDocumentationSerializer).spec

        expect that docs.jsonString contains "foo"
    }

    @Test
    fun `supports binary request types`() {
        val docs = activeService.generateDocs(GsonDocumentationSerializer).spec.parseJson(JsonObject::class.java)
            .getAsJsonObject("paths")
            .getAsJsonObject("/bytearray")
            .getAsJsonObject("post")
            .getAsJsonObject("requestBody")
            .getAsJsonObject("content")

        expect that docs.jsonString contains """"format":"byte"""" contains "octet-stream"
    }

    @Test
    fun `supports binary response types`() {
        val docs = activeService.generateDocs(GsonDocumentationSerializer).spec.parseJson(JsonObject::class.java)
            .getAsJsonObject("paths")
            .getAsJsonObject("/bytearray")
            .getAsJsonObject("post")
            .getAsJsonObject("responses")
            .getAsJsonObject("200")
            .getAsJsonObject("content")

        expect that docs.jsonString contains """"format":"byte"""" contains "octet-stream"
    }

}

private data class Foo1(val foo: String)
private data class GenericResponse<out T>(val data: T)
private data class SampleClass(
    @SerializedName("a_sample")
    val aString: String,
    val someStrings: List<String>
)
