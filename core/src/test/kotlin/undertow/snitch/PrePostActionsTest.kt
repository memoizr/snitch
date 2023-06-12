package undertow.snitch

import com.memoizr.assertk.expect
import com.snitch.me.snitchon.NonEmptyString
import me.snitchon.parameters.query
import org.junit.Test

private val list = mutableListOf<String>()

private val param = query("p", condition = NonEmptyString)

class PrePostActionsTest : BaseTest(routes{
    GET("foo")
        .with(queries(param))
        .copy(before = {
            list.add(it[param] + "One")
        }, after = { req ->
            list.add(req[param] + "Three")
        }
        )
        .isHandledBy {
            list.add(request[param] + "Two")
            "ok".ok
        }

}) {

    @Test
    fun `validates routes`() {
        whenPerform GET "/$root/foo?p=X" expectCode 200 expectBody """"ok""""

        expect that list isEqualTo listOf("XOne", "XTwo", "XThree")
    }
}
