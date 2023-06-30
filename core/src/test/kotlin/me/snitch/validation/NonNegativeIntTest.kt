package me.snitch.validation

import com.snitch.me.snitchon.*
import me.snitch.dsl.InlineSnitchTest
import me.snitch.parameters.query
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NonNegativeIntTest : InlineSnitchTest() {
    @Test
    fun `with positive int is valid`() {
        val param by query(ofNonNegativeInt)
        given {
            GET() withQuery param isHandledBy {
                request[param].ok
            }
        } then {
            GET("/?param=5")
                .expectCode(200)
                .expectBody("""5""")
            GET("/?param=0")
                .expectCode(200)
                .expectBody("""0""")
        }
    }

    @Test
    fun `with string is invalid`() {
        val param by query(ofNonNegativeInt)
        given {
            GET() withQuery param isHandledBy {
                 request[param].ok
            }
        } then {
            GET("/?param=notAnInt")
                .expectCode(400)
                .expect {
                    assertThat(it.body())
                        .contains("notAnInt")
                        .contains(ofNonNegativeInt.description)
                }
        }
    }

    @Test
    fun `with negative int is invalid`() {
        val param by query(ofNonNegativeInt)
        given {
            GET() withQuery param isHandledBy {
                request[param].ok
            }
        } then {
            GET("/?param=-42")
                .expectCode(400)
                .expect {
                    assertThat(it.body())
                        .contains("-42")
                        .contains(ofNonNegativeInt.description)
                }
        }
    }
}

class NonEmptyStringTest : InlineSnitchTest() {
    val param by query(ofNonEmptyString)
    @Test
    fun `non empty string is valid`() {
        given {
            GET() withQuery param isHandledBy {
                request[param].ok
            }
        } then {
            GET("/?param=hey")
                .expectCode(200)
                .expectBody(""""hey"""")
        }
    }

    @Test
    fun `empty string is invalid`() {
        given {
            GET() withQuery param isHandledBy {
                request[param].ok
            }
        } then {
            GET("/?param=")
                .expectCode(400)
                .expect {
                    assertThat(it.body())
                        .contains("``")
                        .contains(ofNonEmptyString.description)
                }
        }
    }
}

class NonEmptyStringSetTest : InlineSnitchTest() {
    val param by query(ofNonEmptyStringSet)

    @Test
    fun `non empty string set is valid`() {
        given {
            GET() withQuery param isHandledBy {
                request[param].ok
            }
        } then {
            GET("/?param=no,yes,no")
                .expectCode(200)
                .expectBody("""["no","yes"]""")
            GET("/?param=no&param=yes&param=no")
                .expectCode(200)
                .expectBody("""["no","yes"]""")
        }
    }

    @Test
    fun `empty string set is invalid`() {
        given {
            GET() withQuery param isHandledBy {
                request[param].ok
            }
        } then {
            GET("/?param=")
                .expectCode(400)
                .expect {
                    assertThat(it.body())
                        .contains("``")
                        .contains(ofNonEmptyStringSet.description)
                }
        }
    }
}
class StringSetTest : InlineSnitchTest() {
    val param by query(ofStringSet)

    @Test
    fun `string set is valid`() {
        given {
            GET() withQuery param isHandledBy {
                request[param].ok
            }
        } then {
            GET("/?param=no,yes,no")
                .expectCode(200)
                .expectBody("""["no","yes"]""")
            GET("/?param=no&param=yes&param=no")
                .expectCode(200)
                .expectBody("""["no","yes"]""")
            GET("/?param=")
                .expectCode(200)
                .expectBody("""[""]""")
            GET("/?param=&param=")
                .expectCode(200)
                .expectBody("""[""]""")
        }
    }
}

class OfEnumTest : InlineSnitchTest() {
    enum class Foo { bar, baz}
    val param by query(ofEnum<Foo>())

    @Test
    fun `enum string is valid`() {
        given {
            GET() withQuery param isHandledBy {
                request[param].ok
            }
        } then {
            GET("/?param=bar")
                .expectCode(200)
                .expectBody(""""bar"""")
            GET("/?param=baz")
                .expectCode(200)
                .expectBody(""""baz"""")
        }
    }

    @Test
    fun `any other string is invalid`() {
        given {
            GET() withQuery param isHandledBy {
                request[param].ok
            }
        } then {
            GET("/?param=lol")
                .expectCode(400)
                .expect {
                    assertThat(it.body())
                        .contains("lol")
                        .contains(ofEnum<Foo>().description)
                }
        }
    }
}

class OfRepeatableEnumTest : InlineSnitchTest() {
    enum class Foo { bar, baz}
    val param by query(ofRepeatableEnum<Foo>())

    @Test
    fun `enum string is valid`() {
        given {
            GET() withQuery param isHandledBy {
                request[param].ok
            }
        } then {
            GET("/?param=bar&param=baz")
                .expectCode(200)
                .expectBody("""["bar","baz"]""")
            GET("/?param=baz,bar")
                .expectCode(200)
                .expectBody("""["baz","bar"]""")
        }
    }

    @Test
    fun `any other string is invalid`() {
        given {
            GET() withQuery param isHandledBy {
                request[param].ok
            }
        } then {
            GET("/?param=lol")
                .expectCode(400)
                .expect {
                    assertThat(it.body())
                        .contains("lol")
                        .contains(ofRepeatableEnum<Foo>().description)
                }
        }
    }
}
