package me.snitch.dsl

import me.snitch.router.Router
import me.snitch.router.routes
import me.snitch.router.decorateWith
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
        given {
            decoration {
                GET("/hello").isHandledBy { "hello".ok }
                "nest" / nestedRoutes
            }
        } then {
            GET("/hello").expectBody(""""hello world"""")
            GET("/nest/deeper/end").expectBody(""""end of the world"""")
        }
    }
}
