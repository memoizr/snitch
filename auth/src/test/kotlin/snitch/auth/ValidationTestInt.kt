package snitch.auth

import com.memoizr.assertk.expect
import org.junit.jupiter.api.Test
import snitch.config.SnitchConfig
import snitch.parsers.GsonJsonParser
import snitch.router.Routes
import snitch.service.RoutedService
import snitch.tests.InlineSnitchTest
import snitch.undertow.UndertowSnitchService

class ValidationTestInt : InlineSnitchTest({ _, port -> testRoutes("", this)(port) }) {
    val userToken = SecurityModule.jwt().newToken(JWTClaims("user", Role.USER))

    @Test
    fun `authenticates requests`() {
        given {
            GET("example") isHandledBy { "hi".ok }
            authenticated {
                GET("authenticated") isHandledBy { "hi".ok }
            }
        } then {
            GET("/example")  expect {
                expect that it.body() contains "hi"
            }
            GET("/authenticated")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.body() contains "hi"
                }
            GET("/authenticated")
                .withHeaders(mapOf("X-Access-Token" to "xxx"))
                .expect {
                expect that it.body() contains
                        "401" contains
                        "unauthorized"
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
