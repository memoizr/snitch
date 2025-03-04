package snitch.tests

import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import snitch.shank.NewProvider0
import snitch.shank.NewProvider1
import snitch.shank.NewProvider2
import snitch.shank.NewProvider3
import snitch.shank.Provider
import snitch.shank.SingleProvider0
import snitch.shank.SingleProvider1
import snitch.shank.SingleProvider2
import snitch.shank.SingleProvider3
import snitch.shank.resetShank
import snitch.tests.Mocks.Companion.internalMocks
import snitch.tests.Mocks.Companion.resetProvider
import kotlin.reflect.KClass


typealias MockConfig = Mocks.() -> Unit

interface HttpTest {
    val mocks: MockConfig get() = {}

    @BeforeEach
    fun before() {
        mocks(Mocks(this::class))
    }

    @AfterEach
    fun after() {
        resetShank()
        internalMocks[this::class]?.forEach { resetProvider(it) }
        internalMocks.clear()
    }

}

context(HttpTest)
inline fun <reified T> SingleProvider0<T>.over(noinline fn: () -> T) = internalMocks.put(
    this@HttpTest::class, (internalMocks[this@HttpTest::class] ?: emptyList())
        .plus(this.override(fn))
)

class Mocks(val host: KClass<*>) {
    inline val <reified T> Provider<*, () -> T>.mock
        get() = internalMocks.put(host, (internalMocks[host] ?: emptyList()).plus(mockProvider(false)))

    inline val <reified T> Provider<*, () -> T>.relaxedMock
        get() = internalMocks.put(host, (internalMocks[host] ?: emptyList()).plus(mockProvider(true)))


    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> Provider<*, () -> T>.mockProvider(relaxed: Boolean): Provider<*, () -> T> {
        when (this) {
            is SingleProvider0<*> -> (this as SingleProvider0<T>).override { mockk(relaxed = relaxed) }
            is SingleProvider1<*, *> -> (this as SingleProvider1<*, T>) override { mockk(relaxed = relaxed) }
            is SingleProvider2<*, *, *> -> (this as SingleProvider2<*, *, T>) override { _, _ -> mockk(relaxed = relaxed) }
            is SingleProvider3<*, *, *, *> -> (this as SingleProvider3<*, *, *, T>) override { _, _, _ -> mockk(relaxed = relaxed) }
            is NewProvider0<*> -> (this as NewProvider0<T>) override { mockk(relaxed = relaxed) }
            is NewProvider1<*, *> -> (this as NewProvider1<*, T>) override { mockk(relaxed = relaxed) }
            is NewProvider2<*, *, *> -> (this as NewProvider2<*, *, T>) override { _, _ -> mockk(relaxed = relaxed) }
            is NewProvider3<*, *, *, *> -> (this as NewProvider3<*, *, *, T>) override { _, _, _ -> mockk(relaxed = relaxed) }
            else -> Unit
        }
        return this
    }


    companion object {
        val internalMocks = mutableMapOf<KClass<*>, List<Provider<*, *>>>()

        fun resetProvider(provider: Provider<*, *>) {
            when (provider) {
                is SingleProvider0<*> -> provider.override(null)
//                is SingleProvider1<*, *> -> provider.override(null)
//                is SingleProvider2<*, *, *> -> provider.override(null)
//                is SingleProvider3<*, *, *, *> -> provider.override(null)
//            is NewProvider0<*> -> (this as NewProvider0<T>) override(null)
//            is NewProvider1<*, *> -> (this as NewProvider1<*, T>) override(null)
//            is NewProvider2<*, *, *> -> (this as NewProvider2<*, *, T>) override(null)
//            is NewProvider3<*, *, *, *> -> (this as NewProvider3<*, *, *, T>) override(null)
                else -> Unit
            }
        }
    }
}
