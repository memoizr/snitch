package me.snitchon

import com.memoizr.assertk.expect
import com.snitch.me.snitchon.NonEmptyString
import me.snitchon.parameters.query
import org.junit.Test

private val list = mutableListOf<String>()

private val param by query(condition = NonEmptyString, "p")

class PrePostActionsTest : BaseTest(testRoutes {
    GET("foo")
        .with(queries(param))
        .doBefore { list.add(this[param] + "One") }
        .doBefore { list.add(this[param] + "And") }
        .doAfter { list.add(this[param] + "Three") }
        .isHandledBy {
            list.add(request[param] + "Two")
            "ok".ok
        }

    applyToAll({
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

}) {

    @Test
    fun `validates routes`() {
        whenPerform GET "/foo?p=X" expectCode 200 expectBody """"ok""""

        expect that list isEqualTo listOf("XAnd", "XOne", "XTwo", "XThree")
        list.clear()
    }

    @Test
    fun `supports global config`() {
        whenPerform GET "/global?p=X" expectCode 200 expectBody """"ok""""

        expect that list isEqualTo listOf("XGlobal", "XOne", "XTwo", "XThree")
    }
}
