package snitch.coroutines

import kotlinx.coroutines.delay
import org.junit.jupiter.api.Test
import snitch.config.SnitchConfig
import snitch.parsers.GsonJsonParser
import snitch.request.parsing
import snitch.router.Routes
import snitch.service.RoutedService
import snitch.tests.Ported
import snitch.tests.TestMethods
import snitch.types.HandlerResponse
import snitch.types.StatusCodes
import snitch.undertow.UndertowSnitchService
import java.util.*
import kotlin.system.measureTimeMillis

class CoroutineSupportTest : InlineSnitchTest() {
    suspend fun doSomething(): String {
        delay(100)
        return "delayed"
    }

    @Test
    fun `supports coroutines`() {
        val blah: HandlerResponse<Nothing, String, StatusCodes.OK> by coHandling {
            doSomething().ok
        }

        val baz: HandlerResponse<String, String, StatusCodes.OK> by parsing<String>() coHandling { doSomething().ok }

        given {
            GET("foo") isHandledBy blah
            POST("baz") with body<String>() isHandledBy baz
            GET("foo") isCoHandledBy { doSomething().ok.plainText }
        } then {
            GET("/foo").expectBody("delayed").expectCode(200)
            val time = measureTimeMillis {
                GET("/foo").expectBody("delayed").expectCode(200)
            }
            assertNear(time, 100L, 30)
        }
    }
}

fun assertNear(value: Long, target: Long, marginPercent: Int) {
    val margin = target * marginPercent / 100
    assert(value in (target - margin)..(target + margin)) {
        "Value $value is not within $marginPercent% of $target"
    }
}

abstract class InlineSnitchTest : Ported, TestMethods {
    override open val port = Random().nextInt(5000) + 2000

    data class RoutedTest(val routedService: RoutedService) {
        fun assert(assertionBlock: () -> Unit) {
            routedService.start()
            try {
                assertionBlock()
            } finally {
                routedService.stop()
            }
        }
    }

    infix fun RoutedService.then(assertionBlock: () -> Unit) {
        this.start()
        try {
            assertionBlock()
        } finally {
            this.stop()
        }
    }

    fun given(routes: Routes) = testRoutes("", routes)(port)
}

fun testRoutes(basePath: String = "", router: Routes): (Int) -> RoutedService = { port ->
    UndertowSnitchService(
        GsonJsonParser,
        SnitchConfig(
            SnitchConfig.Service(
                basePath = basePath,
                port = port
            )
        )
    ).onRoutes(router)
}
