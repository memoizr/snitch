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


private val listHandler by Handler<Nothing, _> {
    listOf(SampleClass("hey", listOf())).ok()
}

class DocumentationTest : SnitchTest(routes {
    GET("one").isHandledBy { SampleClass("hey", listOf()).ok() }
    GET("two").isHandledBy(listHandler)
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

    @After
    override fun after() {
        activeService.stopListening()
    }
}
