package snitch.kofix.customization


import com.memoizr.assertk.expect
import com.memoizr.assertk.isEqualTo
import com.memoizr.assertk.notNull
import com.memoizr.assertk.of
import snitch.kofix.aRandom
import snitch.kofix.any
import snitch.kofix.customize
import snitch.kofix.fixtures.Param0
import snitch.kofix.fixtures.Param1
import snitch.kofix.fixtures.Param2
import snitch.kofix.fixtures.Param3
import snitch.kofix.fixtures.Param3b
import snitch.kofix.fixtures.Param4
import snitch.kofix.fixtures.Param5
import snitch.kofix.fixtures.Param6
import org.junit.Test
import java.time.Instant

data class InvertedGenerics<out A, out B, in C>(private val x: C, val b: B, val a: A)

class CustomizeTest {

    val pairIntInt by aRandom<Pair<Int, Int>>()
    val pairStringListInt by aRandom<Pair<String, List<Int>>>()
    val inverted by aRandom<InvertedGenerics<List<String>, Int, Long>>()

    init {
        customize { Pair(3, 4) }
        customize { Pair("hey", listOf(5)) }
        customize { Param0 }
        customize<Param1<Int>> { Param1(any()) }
        customize { Param2(any<Int>(), 2) }
        customize { Param3(any<Int>(), 2, 3) }
        customize { Param3b(any<Int>(), 2, 3, 33) }
        customize { Param4(any<Int>(), 2, 3, 4) }
        customize { Param5(any<Int>(), 2, 3, 4, 5) }
        customize { Param6(any<Int>(), 2, 3, 4, 5, 6) }

        customize<InvertedGenerics<List<String>, Int, Long>> {
            InvertedGenerics(a<Long>(), 0, a<List<String>>())
        }
    }

    @Test
    fun `works with generics`() {
        expect that pairIntInt isEqualTo Pair(3, 4)
        expect that pairStringListInt isEqualTo Pair("hey", listOf(5))
        expect that inverted _is notNull
        expect that inverted.a.first() isInstance of<String>()
    }

    val p0 by aRandom<Param0>()
    val p1 by aRandom<Param1<Int>>()
    val p2 by aRandom<Param2<Int, Int>>()
    val p3 by aRandom<Param3<Int, Int, Int>>()
    val p3b by aRandom<Param3b<Int, Int, Int>>()
    val p4 by aRandom<Param4<Int, Int, Int, Int>>()
    val p5 by aRandom<Param5<Int, Int, Int, Int, Int>>()

    @Test
    fun `works with different arities`() {
        expect that p0 _is notNull
        expect that p1.t1 isInstance of<Int>()
        expect that p2.t2 isEqualTo 2
        expect that p3.t3 isEqualTo 3
        expect that p3b.t3b isEqualTo 33
        expect that p4.t4 isEqualTo 4
        expect that p5.t5 isEqualTo 5
    }

    private data class MyInstant(val instant: Instant)
    val now = Instant.now()
    private val myInstant by aRandom<MyInstant>()
    @Test
    fun `can customize instant`() {
        customize<Instant> { now }
        myInstant.instant isEqualTo now
    }
}
