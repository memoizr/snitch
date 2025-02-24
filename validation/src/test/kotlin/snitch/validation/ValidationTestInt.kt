package snitch.validation

import com.memoizr.assertk.expect
import jakarta.validation.constraints.Min
import org.junit.jupiter.api.Test
import snitch.config.SnitchConfig
import snitch.parsers.GsonJsonParser
import snitch.router.Routes
import snitch.service.RoutedService
import snitch.tests.InlineSnitchTest
import snitch.undertow.UndertowSnitchService

class ValidationTestInt : InlineSnitchTest({ _, port -> testRoutes("", this)(port) }) {
    data class Example(
        val a: Int,
        val b: Int
    )

    data class ValidatedExample(
        @field:Min(50)
        val a: Int,
        @field:Min(33)
        val b: Int
    )

    @Test
    fun `validates input`() {
        given {
            POST("example") with body<Example>() isHandledBy { "hi".ok }
            validated {
                POST("validated") with body<ValidatedExample>() isHandledBy { "hi".ok }
            }
        } then {
            POST("/example") withBody (Example(0, 0)) expect {
                expect that it.body() contains "hi"
            }
            POST("/validated") withBody (ValidatedExample(0, 0)) expect {
                expect that it.body() contains
                        "a: must be greater than or equal to 50" contains
                        "b: must be greater than or equal to 33"
            }
        }
    }
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
