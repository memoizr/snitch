package snitch.shank

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import shouldBeEqualTo
import shouldBeSameReference
import snitch.shank.ParameterSingletonModule.fiveParam
import snitch.shank.ParameterSingletonModule.fourParam
import snitch.shank.ParameterSingletonModule.noParam
import snitch.shank.ParameterSingletonModule.oneParam
import snitch.shank.ParameterSingletonModule.threeParam
import snitch.shank.ParameterSingletonModule.twoParam
import snitch.shank.SingletonProviderTests.ConcurrentSingleton.nanotime0
import snitch.shank.SingletonProviderTests.ConcurrentSingleton.nanotime1
import snitch.shank.SingletonProviderTests.ConcurrentSingleton.nanotime2
import snitch.shank.SingletonProviderTests.ConcurrentSingleton.nanotime3
import snitch.shank.SingletonProviderTests.ConcurrentSingleton.nanotime4
import snitch.shank.SingletonProviderTests.ConcurrentSingleton.nanotime5

private object ParameterSingletonModule : ShankModule {
    val noParam = single { -> ParamData() }
    val oneParam = single { a: Int -> ParamData(a) }
    val twoParam = single { a: Int, b: Int -> ParamData(a, b) }
    val threeParam = single { a: Int, b: Int, c: Int -> ParamData(a, b, c) }
    val fourParam = single { a: Int, b: Int, c: Int, d: Int -> ParamData(a, b, c, d) }
    val fiveParam = single { a: Int, b: Int, c: Int, d: Int, e: Int -> ParamData(a, b, c, d, e) }
}

class SingletonProviderTests {

    @BeforeEach
    fun setUp() {
        resetShank()
    }

    @Test
    fun `should create and cache instances with no parameters`() {
        noParam() shouldBeEqualTo ParamData()
        noParam() shouldBeSameReference noParam()
    }
    
    @Test
    fun `should create and cache instances with one parameter`() {
        oneParam(1) shouldBeEqualTo ParamData(1)
        oneParam(1) shouldBeSameReference oneParam(1)
    }
    
    @Test
    fun `should create and cache instances with two parameters`() {
        twoParam(1, 2) shouldBeEqualTo ParamData(1, 2)
        twoParam(1, 2) shouldBeSameReference twoParam(1, 2)
    }
    
    @Test
    fun `should create and cache instances with three parameters`() {
        threeParam(1, 2, 3) shouldBeEqualTo ParamData(1, 2, 3)
        threeParam(1, 2, 3) shouldBeSameReference threeParam(1, 2, 3)
    }
    
    @Test
    fun `should create and cache instances with four parameters`() {
        fourParam(1, 2, 3, 4) shouldBeEqualTo ParamData(1, 2, 3, 4)
        fourParam(1, 2, 3, 4) shouldBeSameReference fourParam(1, 2, 3, 4)
    }
    
    @Test
    fun `should create and cache instances with five parameters`() {
        fiveParam(1, 2, 3, 4, 5) shouldBeEqualTo ParamData(1, 2, 3, 4, 5)
        fiveParam(1, 2, 3, 4, 5) shouldBeSameReference fiveParam(1, 2, 3, 4, 5)
    }

    @Test
    fun `should cache different instances for different one parameter values`() {
        oneParam(1) shouldBeSameReference oneParam(1)
        oneParam(2).a shouldBeEqualTo 2
        oneParam(1) shouldNotBeSameReference oneParam(2)
    }
    
    @Test
    fun `should cache different instances for different two parameter values`() {
        twoParam(1, 2) shouldBeEqualTo ParamData(1, 2)
        twoParam(2, 3) shouldBeEqualTo ParamData(2, 3)
        twoParam(1, 2) shouldNotBeSameReference twoParam(2, 3)
    }
    
    @Test
    fun `should cache different instances for different three parameter values`() {
        threeParam(1, 2, 3) shouldBeEqualTo ParamData(1, 2, 3)
        threeParam(2, 3, 4) shouldBeEqualTo ParamData(2, 3, 4)
        threeParam(1, 2, 3) shouldNotBeSameReference threeParam(2, 3, 4)
    }
    
    @Test
    fun `should cache different instances for different four parameter values`() {
        fourParam(1, 2, 3, 4) shouldBeEqualTo ParamData(1, 2, 3, 4)
        fourParam(2, 3, 4, 5) shouldBeEqualTo ParamData(2, 3, 4, 5)
        fourParam(1, 2, 3, 4) shouldNotBeSameReference fourParam(2, 3, 4, 5)
    }
    
    @Test
    fun `should cache different instances for different five parameter values`() {
        fiveParam(1, 2, 3, 4, 5) shouldBeEqualTo ParamData(1, 2, 3, 4, 5)
        fiveParam(2, 3, 4, 5, 6) shouldBeEqualTo ParamData(2, 3, 4, 5, 6)
        fiveParam(1, 2, 3, 4, 5) shouldNotBeSameReference fiveParam(2, 3, 4, 5, 6)
    }

    @Test
    fun `should allow overriding factory implementation with no parameters`() {
        noParam() shouldBeEqualTo ParamData()
        noParam.override { -> ParamData(2) }
        noParam() shouldBeEqualTo ParamData(2)
        noParam.override(null)
        noParam() shouldBeEqualTo ParamData()
    }
    
    @Test
    fun `should allow overriding factory implementation with one parameter`() {
        oneParam(1) shouldBeEqualTo ParamData(1)
        oneParam.override { a: Int -> ParamData(a * 2) }
        oneParam(1) shouldBeEqualTo ParamData(2)
        oneParam.override(null)
        oneParam(1) shouldBeEqualTo ParamData(1)
    }
    
    @Test
    fun `should allow overriding factory implementation with two parameters`() {
        twoParam(1, 2) shouldBeEqualTo ParamData(1, 2)
        twoParam.override { a: Int, b: Int -> ParamData(a * 2, b * 2) }
        twoParam(1, 2) shouldBeEqualTo ParamData(2, 4)
        twoParam.override(null)
        twoParam(1, 2) shouldBeEqualTo ParamData(1, 2)
    }
    
    @Test
    fun `should allow overriding factory implementation with three parameters`() {
        threeParam(1, 2, 3) shouldBeEqualTo ParamData(1, 2, 3)
        threeParam.override { a: Int, b: Int, c: Int -> ParamData(a * 2, b * 2, c * 2) }
        threeParam(1, 2, 3) shouldBeEqualTo ParamData(2, 4, 6)
        threeParam.override(null)
        threeParam(1, 2, 3) shouldBeEqualTo ParamData(1, 2, 3)
    }
    
    @Test
    fun `should allow overriding factory implementation with four parameters`() {
        fourParam(1, 2, 3, 4) shouldBeEqualTo ParamData(1, 2, 3, 4)
        fourParam.override { a: Int, b: Int, c: Int, d: Int ->
            ParamData(
                a * 2,
                b * 2,
                c * 2,
                d * 2
            )
        }
        fourParam(1, 2, 3, 4) shouldBeEqualTo ParamData(2, 4, 6, 8)
        fourParam.override(null)
        fourParam(1, 2, 3, 4) shouldBeEqualTo ParamData(1, 2, 3, 4)
    }
    
    @Test
    fun `should allow overriding factory implementation with five parameters`() {
        fiveParam(1, 2, 3, 4, 5) shouldBeEqualTo ParamData(1, 2, 3, 4, 5)
        fiveParam.override { a: Int, b: Int, c: Int, d: Int, e: Int ->
            ParamData(
                a * 2,
                b * 2,
                c * 2,
                d * 2,
                e * 2
            )
        }
        fiveParam(1, 2, 3, 4, 5) shouldBeEqualTo ParamData(2, 4, 6, 8, 10)
        fiveParam.override(null)
        fiveParam(1, 2, 3, 4, 5) shouldBeEqualTo ParamData(1, 2, 3, 4, 5)
    }

    @Test
    fun `should return same instance when accessed concurrently with no parameters`() {
        runBlocking(Dispatchers.Default) {
            testConcurrentAccess { nanotime0() }
        }
    }
    
    @Test
    fun `should return same instance when accessed concurrently with one parameter`() {
        runBlocking(Dispatchers.Default) {
            testConcurrentAccess { nanotime1(0) }
        }
    }
    
    @Test
    fun `should return same instance when accessed concurrently with two parameters`() {
        runBlocking(Dispatchers.Default) {
            testConcurrentAccess { nanotime2(0, 0) }
        }
    }
    
    @Test
    fun `should return same instance when accessed concurrently with three parameters`() {
        runBlocking(Dispatchers.Default) {
            testConcurrentAccess { nanotime3(0, 0, 0) }
        }
    }
    
    @Test
    fun `should return same instance when accessed concurrently with four parameters`() {
        runBlocking(Dispatchers.Default) {
            testConcurrentAccess { nanotime4(0, 0, 0, 0) }
        }
    }
    
    @Test
    fun `should return same instance when accessed concurrently with five parameters`() {
        runBlocking(Dispatchers.Default) {
            testConcurrentAccess { nanotime5(0, 0, 0, 0, 0) }
        }
    }
    
    private suspend fun <T> testConcurrentAccess(block: () -> T) = coroutineScope {
        val results = List(10) {
            async { block() }
        }.awaitAll()
        
        // All results should be the same instance
        val first = results.first() as Any
        results.forEach { result ->
            result shouldBeSameReference first
        }
    }

    private object ConcurrentSingleton : ShankModule {
        val nanotime0 = single { -> getTimeSlow() }
        val nanotime1 = single { _: Any -> getTimeSlow() }
        val nanotime2 = single { _: Any, _: Any -> getTimeSlow() }
        val nanotime3 = single { _: Any, _: Any, _: Any -> getTimeSlow() }
        val nanotime4 = single { _: Any, _: Any, _: Any, _: Any -> getTimeSlow() }
        val nanotime5 = single { _: Any, _: Any, _: Any, _: Any, _: Any -> getTimeSlow() }

        private inline fun getTimeSlow(): Long {
            Thread.sleep(1)
            return System.nanoTime()
        }
    }
}

infix fun Any?.shouldNotBeSameReference(other: Any?) {
    if (this === other) {
        throw AssertionError("Expected $this to not be the same reference as $other")
    }
}
