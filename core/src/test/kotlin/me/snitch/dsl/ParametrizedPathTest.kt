package me.snitch.dsl

import me.snitch.parameters.path
import org.junit.Test

class ParametrizedPathTest : InlineSnitchTest() {
    val param by path()
    val otherPathParam by path()
    val thirdParam by path("third")

    @Test
    fun `supports single path param GET route`() {
        given {
            GET(param / "there").isHandledBy { request[param].ok }
        }.then {
            GET("/hey/there")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports single path param POST route`() {
        given {
            POST(param / "there").with(body<String>()).isHandledBy { body.ok }
        }.then {
            POST("/hey/there")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports single path param DELETE route`() {
        given {
            DELETE(param / "there").isHandledBy { "ok".ok }
        }.then {
            DELETE("/hey/there")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports single path param PATCH route`() {
        given {
            PATCH(param / "there").isHandledBy { "patch".ok }
        }.then {
            PATCH("/hey/there")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports single path param PUT route`() {
        given {
            PUT(param / "there").isHandledBy { "put".ok }
        }.then {
            PUT("/hey/there")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""put"""")
        }
    }

    @Test
    fun `supports nested GET route with multiple segments and path param`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                GET(param / "nope").isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.then {
            GET("/foo/bar/hey/baz/there/nope")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested POST route with multiple segments and path param`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                POST(param / "nope").with(body<String>())
                    .isHandledBy { "${request[otherPathParam]} ${request[param]} ${body}".ok }
            }
        }.then {
            POST("/foo/bar/hey/baz/there/nope")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey there no"""")
        }
    }

    @Test
    fun `supports nested DELETE route with multiple segments and path param`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                DELETE(param / "nope").isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.then {
            DELETE("/foo/bar/hey/baz/there/nope")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested PATCH route with multiple segments and path param`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                PATCH(param / "nope").isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.then {
            PATCH("/foo/bar/hey/baz/there/nope")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested PUT route with multiple segments and path param`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                PUT(param / "nope").isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.then {
            PUT("/foo/bar/hey/baz/there/nope")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports multiple path params GET route`() {
        given {
            GET(param / "there" / otherPathParam).isHandledBy { "${request[param]} ${request[otherPathParam]}".ok }
        }.then {
            GET("/hey/there/you")
                .expectCode(200)
                .expectBody(""""hey you"""")
        }
    }

    @Test
    fun `supports multiple path params POST route`() {
        given {
            POST(param / "there" / otherPathParam).with(body<String>())
                .isHandledBy { "${request[param]} ${request[otherPathParam]} ${body}".ok }
        }.then {
            POST("/hey/there/you")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey you no"""")
        }
    }

    @Test

    fun `supports multiple path params DELETE route`() {
        given {
            DELETE(param / "there" / otherPathParam).isHandledBy { "${request[param]} ${request[otherPathParam]}".ok }
        }.then {
            DELETE("/hey/there/you")
                .expectCode(200)
                .expectBody(""""hey you"""")
        }
    }

    @Test
    fun `supports multiple path params PATCH route`() {
        given {
            PATCH(param / "there" / otherPathParam).isHandledBy { "${request[param]} ${request[otherPathParam]}".ok }
        }.then {
            PATCH("/hey/there/you")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey you"""")
        }
    }

    @Test
    fun `supports multiple path params PUT route`() {
        given {
            PUT(param / "there" / otherPathParam).isHandledBy { "${request[param]} ${request[otherPathParam]}".ok }
        }.then {
            PUT("/hey/there/you")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey you"""")
        }
    }


    @Test
    fun `supports multiple path params GET route with multiple segments`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                GET(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
            }
        }.then {
            GET("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports multiple path params POST route with multiple segments`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                POST(param / "lol" / thirdParam).with(body<String>())
                    .isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]} ${body}".ok }
            }
        }.then {
            POST("/foo/bar/hey/baz/there/lol/you")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey there you no"""")
        }
    }

    @Test
    fun `supports multiple path params DELETE route with multiple segments`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                DELETE(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
            }
        }.then {
            DELETE("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports multiple path params PATCH route with multiple segments`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                PATCH(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
            }
        }.then {
            PATCH("/foo/bar/hey/baz/there/lol/you")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports multiple path params PUT route with multiple segments`() {
        given {
            "foo" / "bar" / otherPathParam / "baz" / {
                PUT(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
            }
        }.then {
            PUT("/foo/bar/hey/baz/there/lol/you")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params GET route `() {
        given {
            "foo" / {
                "bar" / {
                    otherPathParam / {
                        "baz" / {
                            GET(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                        }
                    }
                }
            }
        }.then {
            GET("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params POST route `() {
        given {
            "foo" / {
                "bar" / {
                    otherPathParam / {
                        "baz" / {
                            POST(param / "lol" / thirdParam).with(body<String>())
                                .isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]} ${body}".ok }
                        }
                    }
                }
            }
        }.then {
            POST("/foo/bar/hey/baz/there/lol/you")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey there you no"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params DELETE route `() {
        given {
            "foo" / {
                "bar" / {
                    otherPathParam / {
                        "baz" / {
                            DELETE(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                        }
                    }
                }
            }
        }.then {
            DELETE("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params PATCH route `() {
        given {
            "foo" / {
                "bar" / {
                    otherPathParam / {
                        "baz" / {
                            PATCH(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                        }
                    }
                }
            }
        }.then {
            PATCH("/foo/bar/hey/baz/there/lol/you")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params PUT route `() {
        given {
            "foo" / {
                "bar" / {
                    otherPathParam / {
                        "baz" / {
                            PUT(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                        }
                    }
                }
            }
        }.then {
            PUT("/foo/bar/hey/baz/there/lol/you")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params GET route with multiple segments`() {
        given {
            "foo" / {
                "bar" / {
                    otherPathParam / "baz" / {
                        GET(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                    }
                }
            }
        }.then {
            GET("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params POST route with multiple segments`() {
        given {
            "foo" / {
                "bar" / {
                    otherPathParam / "baz" / {
                        POST(param / "lol" / thirdParam).with(body<String>())
                            .isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]} ${body}".ok }
                    }
                }
            }
        }.then {
            POST("/foo/bar/hey/baz/there/lol/you")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey there you no"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params DELETE route with multiple segments`() {
        given {
            "foo" / {
                "bar" / {
                    otherPathParam / "baz" / {
                        DELETE(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                    }
                }
            }
        }.then {
            DELETE("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params PATCH route with multiple segments`() {
        given {
            "foo" / {
                "bar" / {
                    otherPathParam / "baz" / {
                        PATCH(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                    }
                }
            }
        }.then {
            PATCH("/foo/bar/hey/baz/there/lol/you")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params PUT route with multiple segments`() {
        given {
            "foo" / {
                "bar" / {
                    otherPathParam / "baz" / {
                        PUT(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                    }
                }
            }
        }.then {
            PUT("/foo/bar/hey/baz/there/lol/you")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }
}