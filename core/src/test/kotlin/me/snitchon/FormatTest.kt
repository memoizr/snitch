package me.snitchon

import com.memoizr.assertk.expect
import me.snitchon.dsl.InlineSnitchTest
import me.snitchon.types.Format.ImageJpeg
import me.snitchon.types.Format.VideoMP4
import org.junit.Test
import java.io.File

class FormatTest : InlineSnitchTest() {

    @Test
    fun `returns json format by default`() {
        withRoutes {
            GET("json") isHandledBy { "ok".ok }
        } assert {
            GET("/json")
                .expect {
                    expect that it.headers().map()["content-type"] isEqualTo listOf("application/json")
                }
        }
    }

    @Test
    fun `returns video mp4 format`() {
        withRoutes {
            GET("bytearray") isHandledBy {
                "ok".ok.format(VideoMP4)
            }
        } assert {
            GET("/bytearray").expect {
                expect that it.headers().map()["content-type"] isEqualTo listOf("video/mp4")
            }
        }

    }

    @Test
    fun `preserves binary formats`() {
        withRoutes {
            GET("image") isHandledBy {
                val readBytes = File(ClassLoader.getSystemClassLoader().getResource("squat.jpg")?.file).readBytes()
                readBytes.ok.format(ImageJpeg)
            }
        } assert {
            GET("/image").expect {
                expect that it.headers().map()["Content-Type"] isEqualTo listOf("image/jpeg")
            }
        }
    }
}
