package me.snitch.validation

import com.snitch.me.snitchon.ofStringSet
import me.snitch.dsl.InlineSnitchTest
import me.snitch.parameters.query
import org.junit.Test

class ParameterValidationTest : InlineSnitchTest() {
    @Test
    fun `supports repeated parameters`() {
        val repeatable by query(ofStringSet)
        given {
            GET("/foo")
                .with(queries(repeatable))
                .isHandledBy {
                    request.get(repeatable).toList().ok
            }
        }.then {
            GET("/foo?repeatable=one&repeatable=two&repeatable=three")
                .expectBody("""["one","two","three"]""")
        }
    }
}