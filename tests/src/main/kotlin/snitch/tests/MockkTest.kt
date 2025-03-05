package snitch.tests

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import snitch.shank.SingleProvider0
import snitch.shank.resetShank
import snitch.tests.Mocks.Companion.internalMocks
import snitch.tests.Mocks.Companion.mockedProviders
import snitch.tests.Mocks.Companion.resetProvider

typealias MockConfig = Mocks.() -> Unit

interface MockkTest {
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
        mockedProviders.clear()
    }
}

context(MockkTest)
inline fun <reified T> SingleProvider0<T>.over(noinline fn: () -> T) = internalMocks.put(
    this@MockkTest::class, (internalMocks[this@MockkTest::class] ?: emptyList())
        .plus(this.override(fn))
)
