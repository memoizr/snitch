package snitch.dsl

import org.junit.jupiter.api.Test
import snitch.router.decorateWith
import snitch.router.plus
import snitch.router.routes

class DecorationTest : InlineSnitchTest() {
    val decoration1 = decorateWith {
            next().map { "${body} one".ok }
        }

    val decoration2 = decorateWith {
            next().map { "${body} two".ok }
        }

    val decoration3 = decorateWith {
            next().map { "${body} three".ok }
        }

    val nestedRoutes = routes {
        "deeper" / {
            GET("/end").isHandledBy { "the".ok }
        }
    }

    @Test
    fun `allows to decorate requests`() {
        given {
            decoration1 {
                GET("/hello").isHandledBy { "hello".ok }
                "nest" / nestedRoutes
            }
        } then {
            GET("/hello").expectBody(""""hello one"""")
            GET("/nest/deeper/end").expectBody(""""the one"""")
        }
    }

    @Test
    fun `support decoration composition`() {
        given {
            (decoration3 + decoration2) {
                decoration1 {
                    GET("/hello").isHandledBy { "hello".ok }
                    "nest" / nestedRoutes
                }
            }
        } then {
            GET("/hello").expectBody(""""hello one two three"""")
            GET("/nest/deeper/end").expectBody(""""the one two three"""")
        }
    }
}
