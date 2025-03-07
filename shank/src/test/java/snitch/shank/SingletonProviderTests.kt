package snitch.shank

import kotlinx.coroutines.CompletableDeferred
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
import java.util.concurrent.atomic.AtomicInteger

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
            val instance = testConcurrentAccess(50) { nanotime0() }
            
            nanotime0() shouldBeSameReference instance
            
            nanotime0.override { -> Nano(System.currentTimeMillis()) }
            val overriddenInstance = testConcurrentAccess(20) { nanotime0() }
            
            overriddenInstance shouldNotBeSameReference instance
            
            nanotime0.override(null)
            val resetInstance = testConcurrentAccess(20) { nanotime0() }
            
            resetInstance shouldNotBeSameReference overriddenInstance
        }
    }
    
    @Test
    fun `should return same instance when accessed concurrently with one parameter`() {
        runBlocking(Dispatchers.Default) {
            val instance = testConcurrentAccess(50) { nanotime1(0) }
            
            nanotime1(0) shouldBeSameReference instance
            
            nanotime1.override { _: Any -> Nano(System.currentTimeMillis()) }
            val overriddenInstance = testConcurrentAccess(20) { nanotime1(0) }
            overriddenInstance shouldNotBeSameReference instance
            
            nanotime1.override(null)
            val resetInstance = testConcurrentAccess(20) { nanotime1(0) }
            resetInstance shouldNotBeSameReference overriddenInstance
        }
    }
    
    @Test
    fun `should return same instance when accessed concurrently with two parameters`() {
        runBlocking(Dispatchers.Default) {
            val instance = testConcurrentAccess(50) { nanotime2(0, 0) }
            
            nanotime2(0, 0) shouldBeSameReference instance
            
            nanotime2.override { _: Any, _: Any -> Nano(System.currentTimeMillis()) }
            val overriddenInstance = testConcurrentAccess(20) { nanotime2(0, 0) }
            overriddenInstance shouldNotBeSameReference instance
            
            nanotime2.override(null)
            val resetInstance = testConcurrentAccess(20) { nanotime2(0, 0) }
            resetInstance shouldNotBeSameReference overriddenInstance
        }
    }
    
    @Test
    fun `should return same instance when accessed concurrently with three parameters`() {
        runBlocking(Dispatchers.Default) {
            val instance = testConcurrentAccess(50) { nanotime3(0, 0, 0) }
            
            nanotime3(0, 0, 0) shouldBeSameReference instance
            
            nanotime3.override { _: Any, _: Any, _: Any -> Nano(System.currentTimeMillis()) }
            val overriddenInstance = testConcurrentAccess(20) { nanotime3(0, 0, 0) }
            overriddenInstance shouldNotBeSameReference instance
            
            nanotime3.override(null)
            val resetInstance = testConcurrentAccess(20) { nanotime3(0, 0, 0) }
            resetInstance shouldNotBeSameReference overriddenInstance
        }
    }
    
    @Test
    fun `should return same instance when accessed concurrently with four parameters`() {
        runBlocking(Dispatchers.Default) {
            val instance = testConcurrentAccess(50) { nanotime4(0, 0, 0, 0) }
            
            nanotime4(0, 0, 0, 0) shouldBeSameReference instance
            
            nanotime4.override { _: Any, _: Any, _: Any, _: Any -> Nano(System.currentTimeMillis()) }
            val overriddenInstance = testConcurrentAccess(20) { nanotime4(0, 0, 0, 0) }
            overriddenInstance shouldNotBeSameReference instance
            
            nanotime4.override(null)
            val resetInstance = testConcurrentAccess(20) { nanotime4(0, 0, 0, 0) }
            resetInstance shouldNotBeSameReference overriddenInstance
        }
    }
    
    @Test
    fun `should return same instance when accessed concurrently with five parameters`() {
        runBlocking(Dispatchers.Default) {
            val instance = testConcurrentAccess(50) { nanotime5(0, 0, 0, 0, 0) }
            
            nanotime5(0, 0, 0, 0, 0) shouldBeSameReference instance
            
            nanotime5.override { _: Any, _: Any, _: Any, _: Any, _: Any -> Nano(System.currentTimeMillis()) }
            val overriddenInstance = testConcurrentAccess(20) { nanotime5(0, 0, 0, 0, 0) }
            overriddenInstance shouldNotBeSameReference instance
            
            nanotime5.override(null)
            val resetInstance = testConcurrentAccess(20) { nanotime5(0, 0, 0, 0, 0) }
            resetInstance shouldNotBeSameReference overriddenInstance
        }
    }
    
    private suspend fun <T> testConcurrentAccess(
        concurrentRequests: Int = 30,
        block: () -> T
    ): T = coroutineScope {
        // Use CountDownLatch-like pattern with a CompletableDeferred
        val readyLatch = AtomicInteger(0)
        val startSignal = CompletableDeferred<Unit>()
        
        val results = List(concurrentRequests) {
            async {
                // Signal ready
                if (readyLatch.incrementAndGet() == concurrentRequests) {
                    // Last thread to arrive releases the barrier
                    startSignal.complete(Unit)
                }
                
                // Wait for all threads to be ready
                startSignal.await()
                
                // Once everyone is ready, execute the block
                block()
            }
        }
        
        // Wait for all results
        val allResults = results.awaitAll()
        
        val first = allResults.first()
        
        allResults.forEachIndexed { index, result ->
            try {
                result shouldBeSameReference first as Any
            } catch (e: AssertionError) {
                throw AssertionError("Concurrent request #$index returned a different instance", e)
            }
        }
        
        return@coroutineScope first
    }

    private suspend fun testOverrideAndInvokeRaceCondition(
        provider: SingleProvider0<Nano>
    ) = coroutineScope {
        val startSignal = CompletableDeferred<Unit>()
        val readyCount = AtomicInteger(0)
        
        val overrider = async {
            if (readyCount.incrementAndGet() == 2) {
                startSignal.complete(Unit)
            }
            startSignal.await()
            provider.override { -> Nano(System.currentTimeMillis()) }
        }
        
        val invoker = async {
            if (readyCount.incrementAndGet() == 2) {
                startSignal.complete(Unit) 
            }
            startSignal.await()
            provider()
        }
        
        overrider.await()
        invoker.await()
    }

    @Test
    fun `should handle race conditions between override and invoke`() {
        runBlocking(Dispatchers.Default) {
            val testProvider = ConcurrentSingleton.nanotime0
            
            repeat(100) {
                testOverrideAndInvokeRaceCondition(testProvider)
                
                testProvider.override(null)
            }
        }
    }
    private data class Nano(val value: Long)

    private object ConcurrentSingleton : ShankModule {
        val nanotime0 = single { -> Nano(getTimeSlow()) }
        val nanotime1 = single { _: Any -> Nano(getTimeSlow()) }
        val nanotime2 = single { _: Any, _: Any -> Nano(getTimeSlow()) }
        val nanotime3 = single { _: Any, _: Any, _: Any -> Nano(getTimeSlow()) }
        val nanotime4 = single { _: Any, _: Any, _: Any, _: Any -> Nano(getTimeSlow()) }
        val nanotime5 = single { _: Any, _: Any, _: Any, _: Any, _: Any -> Nano(getTimeSlow()) }

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
