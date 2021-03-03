package com.snitch

import com.snitch.extensions.print
import com.memoizr.assertk.expect
import org.junit.Rule
import org.junit.Test
import java.io.File

class FormatTest : SparkTest() {

    @Rule
    @JvmField val rule = SparkTestRule(port) {
        GET("json") isHandledBy { "ok".ok }
        GET("bytearray") isHandledBy { "ok".ok.format(Format.VideoMP4) }
        GET("image") isHandledBy { val readBytes = File("../squat.jpg").readBytes()
            readBytes.size.print()
            readBytes.ok.format(Format.ImageJpeg) }
    }

    @Test
    fun `returns correct format`() {
        whenPerform GET "/$root/json" expect {
            expect that it.headers["Content-Type"] isEqualTo "application/json"
        }

        whenPerform GET "/$root/bytearray" expect {
            expect that it.headers["Content-Type"] isEqualTo "video/mp4"
        }
    }

    @Test
    fun `preserves binary formats`() {
        whenPerform GET "/$root/image" expect {
            it.content.size.print()
            it.content.inputStream().bufferedReader().readText().print()
            expect that it.headers["Content-Type"] isEqualTo "image/jpeg"
        }
    }
}
