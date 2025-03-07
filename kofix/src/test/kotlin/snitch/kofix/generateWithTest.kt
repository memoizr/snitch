package snitch.kofix
import com.memoizr.assertk.expect
import org.junit.Test
import snitch.kofix.fixtures.SimpleClass

class generateWithTest {
    init {
        customize { -> listOf("hello")}
        customize { -> listOf(4L, 5L)}
        customize { -> Pair("hello", 33)}
        customize { -> Pair("string", a<List<Map<String, SimpleClass>>>())}
    }

    val anIntList by aRandom<List<Int>>()
    val aLongList by aRandom<List<Long>>()
    val aList by aRandom<List<String>>()
    val pairStringInt by aRandom<Pair<String, Int>>()
    val pairStringListInt by aRandom<Pair<String, List<Map<String, SimpleClass>>>>()

    @Test
    fun `works with generics`() {
        expect that (anIntList == listOf("hello")) _is false
        expect that aList isEqualTo listOf("hello")
        expect that aLongList isEqualTo listOf(4L, 5L)
        expect that pairStringInt isEqualTo Pair("hello", 33)
        expect that pairStringListInt.first isEqualTo  "string"
    }
}