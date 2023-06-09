package com.snitch

import com.memoizr.assertk.expect
import me.snitchon.types.Format.ImageJpeg
import me.snitchon.types.Format.VideoMP4
import me.snitchon.response.format
import me.snitchon.response.ok
import org.junit.Test
import java.io.File

class FormatTest : BaseTest(routes{
    GET("json") isHandledBy { "ok".ok }
    GET("bytearray") isHandledBy {
        "ok".ok.format(VideoMP4) }
    GET("image") isHandledBy { val readBytes = File(ClassLoader.getSystemClassLoader().getResource("squat.jpg")?.file).readBytes()
        readBytes.ok.format(ImageJpeg) }
}) {

    @Test
    fun `returns correct format`() {
        whenPerform GET "/$root/json" expect {
            expect that it.headers().map()["content-type"] isEqualTo listOf("application/json")
        }

        whenPerform GET "/$root/bytearray" expect {
            expect that it.headers().map()["content-type"] isEqualTo listOf("video/mp4")
        }
    }

    @Test
    fun `preserves binary formats`() {
        whenPerform GET "/$root/image" expect {
            expect that it.headers().map()["Content-Type"] isEqualTo listOf("image/jpeg")
        }
    }
}
