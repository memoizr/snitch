package com.snitch

import com.memoizr.assertk.isEqualTo
import me.snitchon.types.Sealed
import me.snitchon.parsers.GsonJsonParser
import org.junit.Test

class ExtensionsTest {
    @Test
    fun `parses sealed classes`() {
        with (GsonJsonParser) {
            val type = "\$type"
            """{"$type":"ONE","one":"hello"}""".parseJson<TheSeal>() isEqualTo TheSeal.ONE("hello")
            """{"$type":"TWO","two":"hello"}""".parseJson<TheSeal>() isEqualTo TheSeal.TWO("hello")

            """{"$type":"ONE","one":"hello"}""".parseJson<TheSeal.ONE>() isEqualTo TheSeal.ONE("hello")
            """{"$type":"TWO","two":"hello"}""".parseJson<TheSeal.TWO>() isEqualTo TheSeal.TWO("hello")
        }
    }
}

sealed class TheSeal : Sealed() {
    data class ONE(val one: String) : TheSeal()
    data class TWO(val two: String) : TheSeal()
}