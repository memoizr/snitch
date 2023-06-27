package me.snitchon.dsl

import me.snitchon.parameters.path
import me.snitchon.router.Routes
import me.snitchon.tests.Ported
import me.snitchon.tests.TestMethods
import java.util.*
import me.snitchon.service.RoutedService
import me.snitchon.testRoutes
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

    infix fun RoutedService.assert(assertionBlock: () -> Unit) {
        this.start()
        try {
            assertionBlock()
        } finally {
            this.stop()
        }
    }

    fun withRoutes(routes: Routes) = testRoutes("", routes)(port)
}

class StringPathTest : InlineSnitchTest() {
    val param by path()

    @Test
    fun `supports simple GET route`() {
        withRoutes {
            GET("/hey").isHandledBy { "hey".ok }
        }.assert {
            GET("/hey")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports simple POST route`() {
        withRoutes {
            POST("/hey").with(body<String>()).isHandledBy { body.ok }
        }.assert {
            POST("/hey")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports simple DELETE route`() {
        withRoutes {
            DELETE("/hey").isHandledBy { "ok".ok }
        }.assert {
            DELETE("/hey")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports simple PATCH route`() {
        withRoutes {
            PATCH("/hey").isHandledBy { "patch".ok }
        }.assert {
            PATCH("/hey")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports simple PUT route`() {
        withRoutes {
            PUT("/hey").isHandledBy { "put".ok }
        }.assert {
            PUT("/hey")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""put"""")
        }
    }

    @Test
    fun `supports nested GET route`() {
        withRoutes {
            "foo" / {
                GET("/hey").isHandledBy { "hey".ok }
            }
        }.assert {
            GET("/foo/hey")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports nested POST route`() {
        withRoutes {
            "foo" / {
                POST("/hey").with(body<String>()).isHandledBy { body.ok }
            }
        }.assert {
            POST("/foo/hey")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports nested DELETE route`() {
        withRoutes {
            "foo" / {
                DELETE("/hey").isHandledBy { "ok".ok }
            }
        }.assert {
            DELETE("/foo/hey")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports nested PATCH route`() {
        withRoutes {
            "foo" / {
                PATCH("/hey").isHandledBy { "patch".ok }
            }
        }.assert {
            PATCH("/foo/hey")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports nested PUT route`() {
        withRoutes {
            "foo" / {
                PUT("/hey").isHandledBy { "put".ok }
            }
        }.assert {
            PUT("/foo/hey")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""put"""")
        }
    }

    @Test
    fun `supports nested GET route with multiple segments`() {
        withRoutes {
            "foo" / "bar" / {
                GET("/hey").isHandledBy { "hey".ok }
            }
        }.assert {
            GET("/foo/bar/hey")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports nested POST route with multiple segments`() {
        withRoutes {
            "foo" / "bar" / {
                POST("/hey").with(body<String>()).isHandledBy { body.ok }
            }
        }.assert {
            POST("/foo/bar/hey")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports nested DELETE route with multiple segments`() {
        withRoutes {
            "foo" / "bar" / {
                DELETE("/hey").isHandledBy { "ok".ok }
            }
        }.assert {
            DELETE("/foo/bar/hey")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports nested PATCH route with multiple segments`() {
        withRoutes {
            "foo" / "bar" / {
                PATCH("/hey").isHandledBy { "patch".ok }
            }
        }.assert {
            PATCH("/foo/bar/hey")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports nested PUT route with multiple segments`() {
        withRoutes {
            "foo" / "bar" / {
                PUT("/hey").isHandledBy { "put".ok }
            }
        }.assert {
            PUT("/foo/bar/hey")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""put"""")
        }
    }

    @Test
    fun `supports nested GET route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / param / {
                GET("/baz").isHandledBy { request[param].ok }
            }
        }.assert {
            GET("/foo/bar/hey/baz")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports nested POST route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / param / {
                POST("/baz").with(body<String>()).isHandledBy { body.ok }
            }
        }.assert {
            POST("/foo/bar/hey/baz")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports nested DELETE route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / param / {
                DELETE("/baz").isHandledBy { "ok".ok }
            }
        }.assert {
            DELETE("/foo/bar/hey/baz")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports nested PATCH route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / param / {
                PATCH("/baz").isHandledBy { "patch".ok }
            }
        }.assert {
            PATCH("/foo/bar/hey/baz")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports nested PUT route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / param / {
                PUT("/baz").isHandledBy { "put".ok }
            }
        }.assert {
            PUT("/foo/bar/hey/baz")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""put"""")
        }
    }
}

