package me.snitchon.dsl

import me.snitchon.parameters.path
import org.junit.Test

class SinglePathParamTest : InlineSnitchTest() {
    val param by path()
    val otherPathParam by path()

    @Test
    fun `supports single path param GET route`() {
        given {
            GET(param).isHandledBy { request[param].ok }
        }.then {
            GET("/hey")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports single path param POST route`() {
        given {
            POST(param).with(body<String>()).isHandledBy { body.ok }
        }.then {
            POST("/hey")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports single path param DELETE route`() {
        given {
            DELETE(param).isHandledBy { "ok".ok }
        }.then {
            DELETE("/hey")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports single path param PATCH route`() {
        given {
            PATCH(param).isHandledBy { "patch".ok }
        }.then {
            PATCH("/hey")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports single path param PUT route`() {
        given {
            PUT(param).isHandledBy { "put".ok }
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
                GET(param).isHandledBy { request[param].ok }
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
                POST(param).with(body<String>()).isHandledBy { body.ok }
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
                DELETE(param).isHandledBy { "ok".ok }
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
                PATCH(param).isHandledBy { "patch".ok }
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
                PUT(param).isHandledBy { "put".ok }
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
                GET(param).isHandledBy { request[param].ok }
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
                POST(param).with(body<String>()).isHandledBy { body.ok }
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
                DELETE(param).isHandledBy { "ok".ok }
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
                PATCH(param).isHandledBy { "patch".ok }
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
                PUT(param).isHandledBy { "put".ok }
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
            "foo" / "bar" / otherPathParam / "baz" / {
                GET(param).isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.then {
            GET("/foo/bar/hey/baz/there")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested POST route with multiple segments and path param`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                POST(param).with(body<String>())
                    .isHandledBy { "${request[otherPathParam]} ${request[param]} ${body}".ok }
            }
        }.then {
            POST("/foo/bar/hey/baz/there")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey there no"""")
        }
    }

    @Test
    fun `supports nested DELETE route with multiple segments and path param`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                DELETE(param).isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.then {
            DELETE("/foo/bar/hey/baz/there")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested PATCH route with multiple segments and path param`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                PATCH(param).isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.then {
            PATCH("/foo/bar/hey/baz/there")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested PUT route with multiple segments and path param`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                PUT(param).isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.then {
            PUT("/foo/bar/hey/baz/there")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

}