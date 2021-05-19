package com.snitch

import com.memoizr.assertk.expect
import org.junit.Rule
import org.junit.Test

class PrePostActionsTest : SparkTest() {

    val list = mutableListOf<String>()

    val param = query("p", condition = NonEmptyString)

    @Rule
    @JvmField
    val rule = SparkTestRule(port) {
        GET("foo")
            .with(queries(param))
            .copy(before = {
                list.add(it[param] + "One")
            }, after = { req, res ->
                list.add(req[param] + "Three")
            }
            )
            .isHandledBy {
                list.add(request[param] + "Two")
                "ok".ok
            }
    }

    @Test
    fun `validates routes`() {
        whenPerform GET "/$root/foo?p=X" expectCode 200 expectBody "okok"

        expect that list isEqualTo listOf("XOne", "XTwo", "XThree")
    }
}
