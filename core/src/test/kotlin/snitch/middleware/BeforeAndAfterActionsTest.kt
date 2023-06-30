package snitch.middleware

import com.memoizr.assertk.expect
import snitch.dsl.InlineSnitchTest
import snitch.parameters.query
import org.junit.Test


class BeforeAndAfterActionsTest : InlineSnitchTest() {
    private val param by query("p")

    @Test
    fun `validates routes`() {
        val list = mutableListOf<String>()
        given {
            GET("foo")
                .with(queries(param))
                .doBefore { list.add(this[param] + "One") }
                .doBefore { list.add(this[param] + "And") }
                .doAfter { list.add(this[param] + "Three") }
                .isHandledBy {
                    list.add(request[param] + "Two")
                    "ok".ok
                }
        } then {
            GET("/foo?p=X").expectCode(200).expectBody(""""ok"""")

            expect that list isEqualTo listOf("XAnd", "XOne", "XTwo", "XThree")
        }
    }

    @Test
    fun `supports global config`() {
        val list = mutableListOf<String>()
        given {
            applyToAll_({
                GET("global")
                    .with(queries(param))
                    .doBefore { list.add(this[param] + "One") }
                    .doAfter {
                        list.add(this[param] + "Three")
                    }
                    .isHandledBy {
                        list.add(request[param] + "Two")
                        "ok".ok
                    }
            }) {
                doBefore { list.add(this[param] + "Global") }
            }
        } then {
            GET("/global?p=X").expectCode(200).expectBody(""""ok"""")
            expect that list isEqualTo listOf("XGlobal", "XOne", "XTwo", "XThree")
        }
    }
}
