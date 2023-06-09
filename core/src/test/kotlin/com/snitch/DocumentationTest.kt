package com.snitch

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.memoizr.assertk.expect
import me.snitchon.Handler
import me.snitchon.documentation.generateDocs
import me.snitchon.ok
import me.snitchon.parsers.GsonDocumentationSerializer
import me.snitchon.parsers.GsonJsonParser.jsonString
import me.snitchon.tests.SnitchTest
import org.junit.Test

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

private val genericHandler by Handler<Nothing, GenericResponse<GenericResponse<Foo1>>> {
    GenericResponse(GenericResponse(Foo1("hey"))).ok
}

class DocumentationTest : SnitchTest(routes {
//    GET("one").isHandledBy { SampleClass("hey", listOf()).ok() }
//    GET("two").isHandledBy(listHandler)
    GET("generic").isHandledBy(genericHandler)
}) {
    @Test
    fun `uses custom serialization`() {
        val docs = Gson().fromJson(
            activeService.generateDocs(GsonDocumentationSerializer).spec,
            com.google.gson.JsonObject::class.java
        )

        expect that docs.jsonString contains "a_sample"
    }

    @Test
    fun `supports generic response types`() {
        val docs = Gson().fromJson(
            activeService.generateDocs(GsonDocumentationSerializer).spec,
            com.google.gson.JsonObject::class.java
        )

        expect that docs.jsonString contains "foo"
    }
}
