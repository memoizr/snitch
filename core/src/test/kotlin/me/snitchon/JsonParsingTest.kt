package me.snitchon

import com.memoizr.assertk.expect
import me.snitchon.parsers.GsonJsonParser
import org.junit.Test


class JsonParsingTest {

    @Test
    fun `it fails with informative error when json is invalid`() {
        with (GsonJsonParser) {
            expect thatThrownBy {
                """{test": maa}""".parseJson<Foo>().test
            } hasMessage """Value of non-nullable member [test] cannot be null"""
        }
    }
}
private data class Foo(val test: String)

