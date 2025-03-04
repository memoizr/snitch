package snitch.test

import com.memoizr.assertk.expect
import io.mockk.every
import org.junit.jupiter.api.Test
import snitch.shank.ShankModule
import snitch.shank.single
import snitch.test.TestModule.one
import snitch.test.TestModule.three
import snitch.test.TestModule.two
import snitch.tests.HttpTest
import snitch.tests.MockConfig
import snitch.tests.over

interface Foo {
    fun bar(): String
}

class One : Foo {
    override fun bar(): String = "one"
}

object TestModule : ShankModule {
    val one = single<Foo> { -> One() }
    val two = single<Foo> { ->
        object : Foo {
            override fun bar(): String = "two"
        }
    }
    val three = single { -> "three" }
}

class HttpTestTest : HttpTest {
    override val mocks: MockConfig = {
        one.relaxedMock
        two.relaxedMock
    }

    @Test
    fun `mocks the values`() {
        expect that one().bar() isEqualTo ""

        every { one().bar() } returns "mocked"

        expect that one().bar() isEqualTo "mocked"
    }

    @Test
    fun `resets the mock`() {
        expect that one().bar() isEqualTo ""

        every { one().bar() } returns "mocked"

        expect that one().bar() isEqualTo "mocked"

        after()

        expect that one().bar() isEqualTo "one"
    }

    @Test
    fun `can handle multiple mocks`() {

        every { one().bar() } returns "one mocked"
        every { two().bar() } returns "two mocked"

        expect that one().bar() isEqualTo "one mocked"
        expect that two().bar() isEqualTo "two mocked"
    }

    @Test
    fun `supports local overrides`() {
        expect that three() isEqualTo "three"

        three.over { "over" }

        expect that three() isEqualTo "over"
    }
}