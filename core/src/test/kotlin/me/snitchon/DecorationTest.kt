package me.snitchon

import me.snitchon.dsl.InlineSnitchTest
import me.snitchon.router.Router
import me.snitchon.router.routes
import me.snitchon.router.decorateWith
import org.junit.Test

class DecorationTest : InlineSnitchTest() {
    val Router.decoration
        get() = decorateWith {
            next().map { "${body} world".ok }
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
