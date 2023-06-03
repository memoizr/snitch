package com.snitch

import com.google.gson.Gson
import com.snitch.documentation.generateDocs
import me.snitchon.tests.SnitchTest
import org.junit.After
import org.junit.Before
import org.junit.Test

private data class SampleClass(val aString: String, val someStrings: List<String>)


private val listHandler by Handler<Nothing, _> {
    listOf(SampleClass("hey", listOf())).ok
}

class DocumentationTest : SnitchTest(routes {
    GET("one").isHandledBy { SampleClass("hey", listOf()).ok }
    GET("two").isHandledBy(listHandler)
}) {

    @Before
    override fun before() {
    }

    @Test
    fun `generates docs`() {
        val docs = Gson().fromJson(activeService.startListening().generateDocs().spec, com.google.gson.JsonObject::class.java)

        println(docs.get("paths").asJsonObject.get("/two"))
    }

    @After
    override fun after() {
        activeService.stopListening()
    }
}