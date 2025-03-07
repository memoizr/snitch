package snitch.documentation

import com.google.gson.internal.LinkedTreeMap
import snitch.parameters.TestResult
import snitch.parsers.GsonJsonParser.parseJson
import snitch.testRoutes
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass

class VisibilityTest : snitch.BaseTest(testRoutes {
    GET("optionally/private") with Visibility.INTERNAL isHandledBy { "Ok".ok }

    GET("default/as/public") isHandledBy {
//        request.queryMap().get().get()
        TestResult("value").ok
    }
}) {


    @Test
    fun `sets visibility for routes`() {
//    expect that ((router.generateDocs().spec.print().parseJson<Json>()["paths"]
//            ?.get("/optionally/private")
//            ?.get("get")
//            ?.get("visibility"))?.div(Visibility::class)) isEqualTo Visibility.INTERNAL
//
//    expect that router.generateDocs().spec.parseJson<Json>().print() /
//            "paths" /
//            "/default/as/public" /
//            "get" /
//            "visibility" /
//            Visibility::class isEqualTo Visibility.PUBLIC
    }
}

data class Json(
    val string: String = "",
    val integer: Int = 0,
    val float: Float = 0f,
    val x: MutableMap<String, Any?> = LinkedTreeMap<String, Any?>()
) : MutableMap<String, Any?> by x {
    override fun get(key: String): Json? {
        return x.get(key)?.let {
            when (it) {
                is String -> Json(string = it)
                is LinkedTreeMap<*, *> -> Json(string = x.toString(), x = it as LinkedTreeMap<String, Any>)
                else -> null
            }
        }
    }

    infix operator fun div(key: String) = get(key)!!

    inline infix operator fun <reified T : Any> div(kclass: KClass<T>): T = string.parseJson<T>()
}

