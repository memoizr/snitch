package com.snitch

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.memoizr.assertk.expect
import com.snitch.documentation.generateDocs
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser.jsonString
import me.snitchon.tests.SnitchTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.reflect.full.starProjectedType

private data class SampleClass(
    @SerializedName("a_sample")
    val aString: String,
    val someStrings: List<String>
)

private data class Foo1(val foo: String)
private data class GenericResponse<out T>(val data: T)

private val listHandler by Handler<Nothing, _> {
    listOf(SampleClass("hey", listOf())).ok()
}

private val genericHandler by Handler<Nothing, _> {
    GenericResponse(Foo1("hey")).ok()
}

class DocumentationTest : SnitchTest(routes {
    GET("one").isHandledBy { SampleClass("hey", listOf()).ok() }
    GET("two").isHandledBy(listHandler)
    GET("generic").isHandledBy(genericHandler)
}) {

    @Before
    override fun before() {
    }

    @Test
    fun `uses custom serialization`() {
        val docs = Gson().fromJson(
            activeService.startListening().generateDocs(GsonDocumentationSerializer).spec,
            com.google.gson.JsonObject::class.java
        )

        expect that docs.jsonString contains "a_sample"
    }

    @Test
    fun `supports generic response types`() {
        val docs = activeService.startListening().generateDocs(GsonDocumentationSerializer).spec

        expect that docs.jsonString contains "foo"
    }

    @After
    override fun after() {
        activeService.stopListening()
    }
}
