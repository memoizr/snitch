package com.snitch

import com.snitch.extensions.print
import com.memoizr.assertk.expect
import me.snitchon.parsers.GsonJsonParser
import org.junit.Test


class JsonParsingTest {

    @Test
    fun `it fails with informative error when json is invalid`() {
        with (GsonJsonParser) {
            expect thatThrownBy {
                """{test": maa}""".parseJson<Foo>().test.print()
            } hasMessage """Value of non-nullable member [test] cannot be null"""
        }
    }
}
data class Foo(val test: String)

