package snitch.dsl

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ResponseHeadersTest : InlineSnitchTest() {
    @Test
    fun `adds headers to response`() {
        given {
            GET() isHandledBy {
                "".ok.plainText.header("key" to "value")
            }
        } then {
            GET("/")
                .expectCode(200)
                .expectBody("")
                .expect {
                    assertThat(it.headers().map()["key"]).isEqualTo(listOf("value"))
                }
        }
    }
}