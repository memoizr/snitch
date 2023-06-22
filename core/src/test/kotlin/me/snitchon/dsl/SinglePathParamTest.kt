package me.snitchon.dsl

import com.snitch.me.snitchon.NonEmptyString
import me.snitchon.parameters.path
import org.junit.Test

class SinglePathParamTest : InlineSnitchTest() {
    val param by path(condition = NonEmptyString)
    val otherPathParam by path(condition = NonEmptyString)

    @Test
    fun `supports single path param GET route`() {
        withRoutes {
            GET(param).isHandledBy { request[param].ok }
        }.assert {
            GET("/hey")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports single path param POST route`() {
        withRoutes {
            POST(param).with(body<String>()).isHandledBy { body.ok }
        }.assert {
            POST("/hey")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports single path param DELETE route`() {
        withRoutes {
            DELETE(param).isHandledBy { "ok".ok }
        }.assert {
            DELETE("/hey")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports single path param PATCH route`() {
        withRoutes {
            PATCH(param).isHandledBy { "patch".ok }
        }.assert {
            PATCH("/hey")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports single path param PUT route`() {
        withRoutes {
            PUT(param).isHandledBy { "put".ok }
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
                GET(param).isHandledBy { request[param].ok }
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
                POST(param).with(body<String>()).isHandledBy { body.ok }
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
                DELETE(param).isHandledBy { "ok".ok }
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
                PATCH(param).isHandledBy { "patch".ok }
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
                PUT(param).isHandledBy { "put".ok }
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
                GET(param).isHandledBy { request[param].ok }
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
                POST(param).with(body<String>()).isHandledBy { body.ok }
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
                DELETE(param).isHandledBy { "ok".ok }
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
                PATCH(param).isHandledBy { "patch".ok }
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
                PUT(param).isHandledBy { "put".ok }
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
            "foo" / "bar" / otherPathParam / "baz" / {
                GET(param).isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.assert {
            GET("/foo/bar/hey/baz/there")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested POST route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                POST(param).with(body<String>())
                    .isHandledBy { "${request[otherPathParam]} ${request[param]} ${body}".ok }
            }
        }.assert {
            POST("/foo/bar/hey/baz/there")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey there no"""")
        }
    }

    @Test
    fun `supports nested DELETE route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                DELETE(param).isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.assert {
            DELETE("/foo/bar/hey/baz/there")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested PATCH route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                PATCH(param).isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.assert {
            PATCH("/foo/bar/hey/baz/there")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested PUT route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                PUT(param).isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.assert {
            PUT("/foo/bar/hey/baz/there")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

}