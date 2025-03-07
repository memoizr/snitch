package snitch.kofix.regressiontests

import com.memoizr.assertk.expect
import com.memoizr.assertk.of
import org.junit.Test
import snitch.kofix.FunkyVisualJava
import snitch.kofix.aRandom
import java.io.File
import java.io.Serializable

class Test2 {
    interface Media : Serializable
    interface Visual : Media
    interface Video : Visual
    interface Audio : Media
    interface Image : Visual
    interface Gif : Image

    data class UriVideo(val uri: String) : Video
    data class FileVideo(val file: File) : Video

    data class UriImage(val uri: String) : Image
    data class OtherImage(val uri: String) : Image

    data class UriGif(val uri: String) : Gif
    data class UriAudio(val uri: String) : Audio

    data class SizedVisual<out T : Media>(val media: T, val size: Size)

    data class FunkyVisual<out V: Video, out I: Image>(val image: I, val y: String, val video: V, val i: I, val v: V)


    data class Clip(val thumbnail: SizedVisual<Image>)

    val aRandomClip by aRandom<Clip>()

    @Test
    fun `media is always an image`() {
        expect that aRandomClip.thumbnail.media isInstance of<Image>()
    }

    val aFunkyVisual by aRandom<FunkyVisual<Video, Image>>{ copy(y = "hello")}
    val aFunkyVisualJava by aRandom<FunkyVisualJava<Video, Image>>()

    @Test
    fun `works with out of order parameters`() {
        expect that aFunkyVisual.image isInstance of<Image>()
        expect that aFunkyVisual.i isInstance of<Image>()

        expect that aFunkyVisual.video isInstance of<Video>()
        expect that aFunkyVisual.v isInstance of<Video>()
        expect that aFunkyVisual.y isInstance of<String>()
        expect that aFunkyVisualJava.image isInstance of<Image>()
        expect that aFunkyVisual.y isEqualTo "hello"
    }
}