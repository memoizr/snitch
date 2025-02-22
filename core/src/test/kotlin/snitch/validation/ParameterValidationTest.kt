package snitch.validation

import snitch.validation.ofStringSet
import snitch.dsl.InlineSnitchTest
import snitch.parameters.query
import org.junit.jupiter.api.Test

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