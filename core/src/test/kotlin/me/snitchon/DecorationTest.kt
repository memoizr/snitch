package me.snitchon

import me.snitchon.dsl.InlineSnitchTest
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.response.SuccessfulHttpResponse
import me.snitchon.router.Router
import me.snitchon.router.Routes
import me.snitchon.router.routes
import me.snitchon.validation.UnregisteredParamException
import org.junit.Test

class DecorationTest : InlineSnitchTest() {
    val Router.decoration
        get() = decorateAll {
            decorate {
                it().map { "${body} world".ok }
            }
        }
    val nestedRoutes = routes {
        "deeper" / {
            GET("/end").isHandledBy { "end of the".ok }
        }
    }

    @Test
    fun `allows to decorate requests`() {
        withRoutes {
            decoration {
                GET("/hello").isHandledBy { "hello".ok }
                "nest" / nestedRoutes
            }
        }

        GET("/hello").expectBody(""""hello world"""")
        GET("/nest/deeper/end").expectBody(""""end of the world"""")
    }
}
