package snitch.tests

import io.mockk.mockk
import snitch.shank.NewProvider0
import snitch.shank.NewProvider1
import snitch.shank.NewProvider2
import snitch.shank.NewProvider3
import snitch.shank.Provider
import snitch.shank.SingleProvider0
import snitch.shank.SingleProvider1
import snitch.shank.SingleProvider2
import snitch.shank.SingleProvider3
import kotlin.reflect.KClass

class Mocks(val host: KClass<*>) {
    inline val <reified T : Any> Provider<T, *>.mock
        get() = internalMocks.put(host, (internalMocks[host] ?: emptyList()).plus(mockProvider(false)))

    inline val <reified T : Any> Provider<T, *>.relaxedMock
        get() = internalMocks.put(host, (internalMocks[host] ?: emptyList()).plus(mockProvider(true)))


    inline fun <reified T : Any> Provider<T, *>.mockProvider(relaxed: Boolean): Provider<T, *> {
        when (this) {
            is SingleProvider0<*> -> (this as SingleProvider0<T>).override { mockk(relaxed = relaxed) }
            is SingleProvider1<*, *> -> (this as SingleProvider1<*, T>) override { mockk(relaxed = relaxed) }
            is SingleProvider2<*, *, *> -> (this as SingleProvider2<*, *, T>) override { _, _ -> mockk(relaxed = relaxed) }
            is SingleProvider3<*, *, *, *> -> (this as SingleProvider3<*, *, *, T>) override { _, _, _ -> mockk(relaxed = relaxed) }
            is NewProvider0<*> -> (this as NewProvider0<T>) override { cachedMock<T>(relaxed) }
            is NewProvider1<*, *> -> (this as NewProvider1<*, T>) override { cachedMock<T>(relaxed) }
            is NewProvider2<*, *, *> -> (this as NewProvider2<*, *, T>) override { _, _ -> cachedMock<T>(relaxed) }
            is NewProvider3<*, *, *, *> -> (this as NewProvider3<*, *, *, T>) override { _, _, _ -> cachedMock<T>(relaxed) }
            else -> Unit
        }
        return this
    }

    inline fun <reified T : Any> Provider<T, *>.cachedMock(relaxed: Boolean): T =
        mockedProviders[this] as? T ?: mockk<T>(relaxed = relaxed).also { mockedProviders.put(this, it)}


    companion object {
        val internalMocks = mutableMapOf<KClass<*>, List<Provider<*, *>>>()
        val mockedProviders = mutableMapOf<Provider<*, *>, Any>()

        fun resetProvider(provider: Provider<*, *>) {
            when (provider) {
                is SingleProvider0<*> -> provider.override(null)
                is SingleProvider1<*, *> -> provider.override(null)
                is SingleProvider2<*, *, *> -> provider.override(null)
                is SingleProvider3<*, *, *, *> -> provider.override(null)
                is NewProvider0<*> -> provider.override(null)
                is NewProvider1<*, *> -> provider.override(null)
                is NewProvider2<*, *, *> -> provider.override(null)
                is NewProvider3<*, *, *, *> -> provider.override(null)
                else -> Unit
            }
        }
    }
}