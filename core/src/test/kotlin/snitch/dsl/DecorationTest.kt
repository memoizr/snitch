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

    val identityDecoration = decorateWith { next() }

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

    @Test
    fun `decoration composition is associative`() {
        given {
            // (decoration1 + decoration2) + decoration3
            ((decoration3 + decoration2) + decoration1) {
                GET("/hello").isHandledBy { "hello".ok }
            }
        } then {
            GET("/hello").expectBody(""""hello one two three"""")
        }

        given {
            // decoration1 + (decoration2 + decoration3)
            (decoration3 + (decoration2 + decoration1)) {
                GET("/hello").isHandledBy { "hello".ok }
            }
        } then {
            GET("/hello").expectBody(""""hello one two three"""")
        }
    }

    @Test
    fun `identity decoration has no effect when composed`() {
        given {
            (identityDecoration + decoration1) {
                GET("/hello").isHandledBy { "hello".ok }
            }
        } then {
            GET("/hello").expectBody(""""hello one"""")
        }

        given {
            (decoration1 + identityDecoration) {
                GET("/hello").isHandledBy { "hello".ok }
            }
        } then {
            GET("/hello").expectBody(""""hello one"""")
        }
    }

    @Test
    fun `supports complex nested composition patterns`() {
        given {
            // (decoration1 + decoration2) { decoration3 { handler } }
            (decoration1 + decoration2) {
                decoration3 {
                    GET("/hello").isHandledBy { "hello".ok }
                }
            }
        } then {
            // The order should be: handler -> decoration3 -> decoration2 -> decoration1
            GET("/hello").expectBody(""""hello three two one"""")
        }
    }

    @Test
    fun `composition applies decorations from right to left`() {
        given {
            (decoration1 + decoration2 + decoration3) {
                GET("/hello").isHandledBy { "hello".ok }
            }
        } then {
            // The order should be: handler -> decoration3 -> decoration2 -> decoration1
            GET("/hello").expectBody(""""hello three two one"""")
        }
    }
}
