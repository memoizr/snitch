package com.snitch

import com.snitch.extensions.print
import com.memoizr.assertk.expect
import org.junit.Test
import java.io.File

class FormatTest : BaseTest(routes{
    GET("json") isHandledBy { "ok".ok }
    GET("bytearray") isHandledBy { "ok".ok.format(Format.VideoMP4) }
    GET("image") isHandledBy { val readBytes = File(ClassLoader.getSystemClassLoader().getResource("squat.jpg")?.file).readBytes()
        readBytes.size.print()
        readBytes.ok.format(Format.ImageJpeg) }
}) {

    @Test
    fun `returns correct format`() {
        whenPerform GET "/$root/json" expect {
            expect that it.headers().map()["Content-Type"] isEqualTo listOf("application/json")
        }

        whenPerform GET "/$root/bytearray" expect {
            expect that it.headers().map()["Content-Type"] isEqualTo listOf("video/mp4")
        }
    }

    @Test
    fun `preserves binary formats`() {
        whenPerform GET "/$root/image" expect {
//            it.body()
//            it.content.size.print()
//            it.content.inputStream().bufferedReader().readText().print()
            expect that it.headers().map()["Content-Type"] isEqualTo listOf("image/jpeg")
        }
    }
}
