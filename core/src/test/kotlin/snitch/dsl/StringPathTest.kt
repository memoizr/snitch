package snitch.dsl

import snitch.parameters.path
import snitch.router.Routes
import snitch.tests.Ported
import snitch.tests.TestMethods
import java.util.*
import snitch.service.RoutedService
import snitch.testRoutes
import org.junit.Test

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

class StringPathTest : InlineSnitchTest() {
    val param by path()

    @Test
    fun `supports simple GET route`() {
        given {
            GET("/hey").isHandledBy { "hey".ok }
        }.then {
            GET("/hey")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports simple POST route`() {
        given {
            POST("/hey").with(body<String>()).isHandledBy { body.ok }
        }.then {
            POST("/hey")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports simple DELETE route`() {
        given {
            DELETE("/hey").isHandledBy { "ok".ok }
        }.then {
            DELETE("/hey")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports simple PATCH route`() {
        given {
            PATCH("/hey").isHandledBy { "patch".ok }
        }.then {
            PATCH("/hey")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports simple PUT route`() {
        given {
            PUT("/hey").isHandledBy { "put".ok }
        }.then {
            PUT("/hey")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""put"""")
        }
    }

    @Test
    fun `supports nested GET route`() {
        given {
            "foo" / {
                GET("/hey").isHandledBy { "hey".ok }
            }
        }.then {
            GET("/foo/hey")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports nested POST route`() {
        given {
            "foo" / {
                POST("/hey").with(body<String>()).isHandledBy { body.ok }
            }
        }.then {
            POST("/foo/hey")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports nested DELETE route`() {
        given {
            "foo" / {
                DELETE("/hey").isHandledBy { "ok".ok }
            }
        }.then {
            DELETE("/foo/hey")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports nested PATCH route`() {
        given {
            "foo" / {
                PATCH("/hey").isHandledBy { "patch".ok }
            }
        }.then {
            PATCH("/foo/hey")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports nested PUT route`() {
        given {
            "foo" / {
                PUT("/hey").isHandledBy { "put".ok }
            }
        }.then {
            PUT("/foo/hey")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""put"""")
        }
    }

    @Test
    fun `supports nested GET route with multiple segments`() {
        given {
            "foo" / "bar" / {
                GET("/hey").isHandledBy { "hey".ok }
            }
        }.then {
            GET("/foo/bar/hey")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports nested POST route with multiple segments`() {
        given {
            "foo" / "bar" / {
                POST("/hey").with(body<String>()).isHandledBy { body.ok }
            }
        }.then {
            POST("/foo/bar/hey")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports nested DELETE route with multiple segments`() {
        given {
            "foo" / "bar" / {
                DELETE("/hey").isHandledBy { "ok".ok }
            }
        }.then {
            DELETE("/foo/bar/hey")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports nested PATCH route with multiple segments`() {
        given {
            "foo" / "bar" / {
                PATCH("/hey").isHandledBy { "patch".ok }
            }
        }.then {
            PATCH("/foo/bar/hey")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports nested PUT route with multiple segments`() {
        given {
            "foo" / "bar" / {
                PUT("/hey").isHandledBy { "put".ok }
            }
        }.then {
            PUT("/foo/bar/hey")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""put"""")
        }
    }

    @Test
    fun `supports nested GET route with multiple segments and path param`() {
        given {
            "foo" / "bar" / param / {
                GET("/baz").isHandledBy { request[param].ok }
            }
        }.then {
            GET("/foo/bar/hey/baz")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports nested POST route with multiple segments and path param`() {
        given {
            "foo" / "bar" / param / {
                POST("/baz").with(body<String>()).isHandledBy { body.ok }
            }
        }.then {
            POST("/foo/bar/hey/baz")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports nested DELETE route with multiple segments and path param`() {
        given {
            "foo" / "bar" / param / {
                DELETE("/baz").isHandledBy { "ok".ok }
            }
        }.then {
            DELETE("/foo/bar/hey/baz")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports nested PATCH route with multiple segments and path param`() {
        given {
            "foo" / "bar" / param / {
                PATCH("/baz").isHandledBy { "patch".ok }
            }
        }.then {
            PATCH("/foo/bar/hey/baz")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports nested PUT route with multiple segments and path param`() {
        given {
            "foo" / "bar" / param / {
                PUT("/baz").isHandledBy { "put".ok }
            }
        }.then {
            PUT("/foo/bar/hey/baz")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""put"""")
        }
    }
}

