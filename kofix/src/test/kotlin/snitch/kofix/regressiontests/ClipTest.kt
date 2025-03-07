package snitch.kofix.regressiontests

import com.memoizr.assertk.expect
import com.memoizr.assertk.notNull
import org.junit.Test
import snitch.kofix.aRandom
import java.io.File
import java.io.Serializable

class Test {
    val clip by aRandom<Clip>()

    @Test
    fun aTest() {
        expect that clip _is notNull
    }

}

data class Clip(
    val clipId: ClipId,
    val thumbnail: Image,
    val gif: Gif,
    val video: Video,
    val badge: Badge,
    val category: Category,
    val tags: List<Tag>,
    val rightsHolder: RightsHolder
) : Serializable

data class Size(val width: Int, val height: Int) : Serializable {
    fun heightAspect() = height.toFloat() / width
    fun widthAspect() = width.toFloat() / height
}

data class ClipId(val id: String) : Serializable
data class RightsHolder(val name: String) : Serializable

interface Media : Serializable {
    val size: Size
}

interface Video : Media
interface Image : Media
interface Gif : Image
data class UriVideo(val uri: String, override val size: Size) : Video
data class FileVideo(val file: File, override val size: Size) : Video
data class UriImage(val uri: String, override val size: Size) : Image
data class UriGif(val uri: String, override val size: Size) : Gif

data class CategoryId(val id: String) : Serializable
data class Category(val categoryId: CategoryId, val name: String) : Serializable

enum class Badge { NONE, HOT, NEW }

sealed class Tag : Query {
    abstract val category: Category
    abstract val name: String
}

data class TextTag(override val category: Category, override val name: String) : Tag() {
    override val query = name
}

data class ImageTag(override val category: Category, override val name: String, val image: Image) : Tag() {
    override val query = name
}

interface Query : Serializable {
    val query: String
    fun isNotEmpty(): Boolean = query.isNotEmpty()
    fun isEmpty(): Boolean = query.isEmpty()
}
