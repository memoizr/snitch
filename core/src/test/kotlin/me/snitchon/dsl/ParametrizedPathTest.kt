package me.snitchon.dsl

import com.snitch.me.snitchon.NonEmptyString
import me.snitchon.parameters.path
import org.junit.Test

class ParametrizedPathTest : InlineSnitchTest() {
    val param = path("param", condition = NonEmptyString)
    val otherPathParam = path("otherPathParam", condition = NonEmptyString)
    val thirdParam = path("third", condition = NonEmptyString)

    @Test
    fun `supports single path param GET route`() {
        withRoutes {
            GET(param / "there").isHandledBy { request[param].ok }
        }.assert {
            GET("/hey/there")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `supports single path param POST route`() {
        withRoutes {
            POST(param / "there").with(body<String>()).isHandledBy { body.ok }
        }.assert {
            POST("/hey/there")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""no"""")
        }
    }

    @Test
    fun `supports single path param DELETE route`() {
        withRoutes {
            DELETE(param / "there").isHandledBy { "ok".ok }
        }.assert {
            DELETE("/hey/there")
                .expectCode(200)
                .expectBody(""""ok"""")
        }
    }

    @Test
    fun `supports single path param PATCH route`() {
        withRoutes {
            PATCH(param / "there").isHandledBy { "patch".ok }
        }.assert {
            PATCH("/hey/there")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""patch"""")
        }
    }

    @Test
    fun `supports single path param PUT route`() {
        withRoutes {
            PUT(param / "there").isHandledBy { "put".ok }
        }.assert {
            PUT("/hey/there")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""put"""")
        }
    }

    @Test
    fun `supports nested GET route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                GET(param / "nope").isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.assert {
            GET("/foo/bar/hey/baz/there/nope")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested POST route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                POST(param / "nope").with(body<String>())
                    .isHandledBy { "${request[otherPathParam]} ${request[param]} ${body}".ok }
            }
        }.assert {
            POST("/foo/bar/hey/baz/there/nope")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey there no"""")
        }
    }

    @Test
    fun `supports nested DELETE route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                DELETE(param / "nope").isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.assert {
            DELETE("/foo/bar/hey/baz/there/nope")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested PATCH route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                PATCH(param / "nope").isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.assert {
            PATCH("/foo/bar/hey/baz/there/nope")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports nested PUT route with multiple segments and path param`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                PUT(param / "nope").isHandledBy { "${request[otherPathParam]} ${request[param]}".ok }
            }
        }.assert {
            PUT("/foo/bar/hey/baz/there/nope")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey there"""")
        }
    }

    @Test
    fun `supports multiple path params GET route`() {
        withRoutes {
            GET(param / "there" / otherPathParam).isHandledBy { "${request[param]} ${request[otherPathParam]}".ok }
        }.assert {
            GET("/hey/there/you")
                .expectCode(200)
                .expectBody(""""hey you"""")
        }
    }

    @Test
    fun `supports multiple path params POST route`() {
        withRoutes {
            POST(param / "there" / otherPathParam).with(body<String>())
                .isHandledBy { "${request[param]} ${request[otherPathParam]} ${body}".ok }
        }.assert {
            POST("/hey/there/you")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey you no"""")
        }
    }

    @Test

    fun `supports multiple path params DELETE route`() {
        withRoutes {
            DELETE(param / "there" / otherPathParam).isHandledBy { "${request[param]} ${request[otherPathParam]}".ok }
        }.assert {
            DELETE("/hey/there/you")
                .expectCode(200)
                .expectBody(""""hey you"""")
        }
    }

    @Test
    fun `supports multiple path params PATCH route`() {
        withRoutes {
            PATCH(param / "there" / otherPathParam).isHandledBy { "${request[param]} ${request[otherPathParam]}".ok }
        }.assert {
            PATCH("/hey/there/you")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey you"""")
        }
    }

    @Test
    fun `supports multiple path params PUT route`() {
        withRoutes {
            PUT(param / "there" / otherPathParam).isHandledBy { "${request[param]} ${request[otherPathParam]}".ok }
        }.assert {
            PUT("/hey/there/you")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey you"""")
        }
    }


    @Test
    fun `supports multiple path params GET route with multiple segments`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                GET(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
            }
        }.assert {
            GET("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports multiple path params POST route with multiple segments`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                POST(param / "lol" / thirdParam).with(body<String>())
                    .isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]} ${body}".ok }
            }
        }.assert {
            POST("/foo/bar/hey/baz/there/lol/you")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey there you no"""")
        }
    }

    @Test
    fun `supports multiple path params DELETE route with multiple segments`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                DELETE(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
            }
        }.assert {
            DELETE("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports multiple path params PATCH route with multiple segments`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                PATCH(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
            }
        }.assert {
            PATCH("/foo/bar/hey/baz/there/lol/you")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports multiple path params PUT route with multiple segments`() {
        withRoutes {
            "foo" / "bar" / otherPathParam / "baz" / {
                PUT(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
            }
        }.assert {
            PUT("/foo/bar/hey/baz/there/lol/you")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params GET route `() {
        withRoutes {
            "foo" / {
                "bar" / {
                    otherPathParam / {
                        "baz" / {
                            GET(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                        }
                    }
                }
            }
        }.assert {
            GET("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params POST route `() {
        withRoutes {
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
        }.assert {
            POST("/foo/bar/hey/baz/there/lol/you")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey there you no"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params DELETE route `() {
        withRoutes {
            "foo" / {
                "bar" / {
                    otherPathParam / {
                        "baz" / {
                            DELETE(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                        }
                    }
                }
            }
        }.assert {
            DELETE("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params PATCH route `() {
        withRoutes {
            "foo" / {
                "bar" / {
                    otherPathParam / {
                        "baz" / {
                            PATCH(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                        }
                    }
                }
            }
        }.assert {
            PATCH("/foo/bar/hey/baz/there/lol/you")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params PUT route `() {
        withRoutes {
            "foo" / {
                "bar" / {
                    otherPathParam / {
                        "baz" / {
                            PUT(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                        }
                    }
                }
            }
        }.assert {
            PUT("/foo/bar/hey/baz/there/lol/you")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params GET route with multiple segments`() {
        withRoutes {
            "foo" / {
                "bar" / {
                    otherPathParam / "baz" / {
                        GET(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                    }
                }
            }
        }.assert {
            GET("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params POST route with multiple segments`() {
        withRoutes {
            "foo" / {
                "bar" / {
                    otherPathParam / "baz" / {
                        POST(param / "lol" / thirdParam).with(body<String>())
                            .isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]} ${body}".ok }
                    }
                }
            }
        }.assert {
            POST("/foo/bar/hey/baz/there/lol/you")
                .withBody("no")
                .expectCode(200)
                .expectBody(""""hey there you no"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params DELETE route with multiple segments`() {
        withRoutes {
            "foo" / {
                "bar" / {
                    otherPathParam / "baz" / {
                        DELETE(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                    }
                }
            }
        }.assert {
            DELETE("/foo/bar/hey/baz/there/lol/you")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params PATCH route with multiple segments`() {
        withRoutes {
            "foo" / {
                "bar" / {
                    otherPathParam / "baz" / {
                        PATCH(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                    }
                }
            }
        }.assert {
            PATCH("/foo/bar/hey/baz/there/lol/you")
                .withBody("patch")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }

    @Test
    fun `supports deeply nested multiple path params PUT route with multiple segments`() {
        withRoutes {
            "foo" / {
                "bar" / {
                    otherPathParam / "baz" / {
                        PUT(param / "lol" / thirdParam).isHandledBy { "${request[otherPathParam]} ${request[param]} ${request[thirdParam]}".ok }
                    }
                }
            }
        }.assert {
            PUT("/foo/bar/hey/baz/there/lol/you")
                .withBody("put")
                .expectCode(200)
                .expectBody(""""hey there you"""")
        }
    }
}