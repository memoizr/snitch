package com.snitch

import me.snitchon.extensions.print
import com.memoizr.assertk.expect
import me.snitchon.Format.ImageJpeg
import me.snitchon.Format.VideoMP4
import me.snitchon.format
import me.snitchon.ok
import org.junit.Test
import java.io.File

class FormatTest : BaseTest(routes{
    GET("json") isHandledBy { "ok".ok }
    GET("bytearray") isHandledBy {
        "ok".print().ok.print().format(VideoMP4).print() }
    GET("image") isHandledBy { val readBytes = File(ClassLoader.getSystemClassLoader().getResource("squat.jpg")?.file).readBytes()
        readBytes.size.print()
        readBytes.ok.format(ImageJpeg) }
}) {

    @Test
    fun `returns correct format`() {
        whenPerform GET "/$root/json" expect {
            println(it.headers())
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
