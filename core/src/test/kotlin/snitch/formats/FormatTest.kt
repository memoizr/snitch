package snitch.formats

import com.memoizr.assertk.expect
import snitch.dsl.InlineSnitchTest
import snitch.types.Format.ImageJpeg
import snitch.types.Format.VideoMP4
import org.junit.Test
import java.io.File

class FormatTest : InlineSnitchTest() {

    @Test
    fun `returns json format by default`() {
        given {
            GET("json") isHandledBy { "ok".ok }
        } then {
            GET("/json")
                .expect {
                    expect that it.headers().map()["content-type"] isEqualTo listOf("application/json")
                }
        }
    }

    @Test
    fun `returns video mp4 format`() {
        given {
            GET("bytearray") isHandledBy {
                "ok".ok.format(VideoMP4)
            }
        } then {
            GET("/bytearray").expect {
                expect that it.headers().map()["content-type"] isEqualTo listOf("video/mp4")
            }
        }

    }

    @Test
    fun `preserves binary formats`() {
        given {
            GET("image") isHandledBy {
                val readBytes = File(ClassLoader.getSystemClassLoader().getResource("squat.jpg")?.file).readBytes()
                readBytes.ok.format(ImageJpeg)
            }
        } then {
            GET("/image").expect {
                expect that it.headers().map()["Content-Type"] isEqualTo listOf("image/jpeg")
            }
        }
    }
}
