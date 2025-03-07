package snitch.test

import com.memoizr.assertk.expect
import io.mockk.every
import org.junit.jupiter.api.Test
import snitch.shank.ShankModule
import snitch.shank.new
import snitch.shank.single
import snitch.test.TestModule.newOne
import snitch.test.TestModule.newParameterized
import snitch.test.TestModule.newThreeParams
import snitch.test.TestModule.newTwoParams
import snitch.test.TestModule.one
import snitch.test.TestModule.parameterized
import snitch.test.TestModule.three
import snitch.test.TestModule.threeParams
import snitch.test.TestModule.two
import snitch.test.TestModule.twoParams
import snitch.tests.MockConfig
import snitch.tests.MockkTest
import snitch.tests.over

interface Foo {
    fun bar(): String
}

class One : Foo {
    override fun bar(): String = "one"
}

object TestModule : ShankModule {
    // SingleProvider0
    val one = single<Foo> { -> One() }
    val two = single<Foo> { ->
        object : Foo {
            override fun bar(): String = "two"
        }
    }
    val three = single { -> "three" }
    
    // SingleProvider1
    val parameterized = single<String, Foo> { param ->
        object : Foo {
            override fun bar(): String = "param: $param"
        }
    }
    
    // SingleProvider2
    val twoParams = single<String, Int, Foo> { param1, param2 ->
        object : Foo {
            override fun bar(): String = "params: $param1, $param2"
        }
    }
    
    // SingleProvider3
    val threeParams = single<String, Int, Boolean, Foo> { param1, param2, param3 ->
        object : Foo {
            override fun bar(): String = "params: $param1, $param2, $param3"
        }
    }
    
    // NewProvider0
    val newOne = new<Foo> { -> One() }
    
    // NewProvider1
    val newParameterized = new<String, Foo> { param ->
        object : Foo {
            override fun bar(): String = "new param: $param"
        }
    }
    
    // NewProvider2
    val newTwoParams = new<String, Int, Foo> { param1, param2 ->
        object : Foo {
            override fun bar(): String = "new params: $param1, $param2"
        }
    }
    
    // NewProvider3
    val newThreeParams = new<String, Int, Boolean, Foo> { param1, param2, param3 ->
        object : Foo {
            override fun bar(): String = "new params: $param1, $param2, $param3"
        }
    }
}

class HttpTestTest : MockkTest {
    override val mocks: MockConfig = {
        one.relaxedMock
        two.relaxedMock
        parameterized.relaxedMock
        twoParams.relaxedMock
        threeParams.relaxedMock
        newParameterized.relaxedMock
        newOne.relaxedMock
        newTwoParams.relaxedMock
        newThreeParams.relaxedMock
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

        mockAfter()

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
    
    @Test
    fun `mocks SingleProvider1`() {
        expect that parameterized("test").bar() isEqualTo ""
        
        every { parameterized("test").bar() } returns "mocked with param"
        
        expect that parameterized("test").bar() isEqualTo "mocked with param"
    }
    
    @Test
    fun `mocks SingleProvider2`() {
        expect that twoParams("test", 42).bar() isEqualTo ""
        
        every { twoParams("test", 42).bar() } returns "mocked with two params"
        
        expect that twoParams("test", 42).bar() isEqualTo "mocked with two params"
    }
    
    @Test
    fun `mocks SingleProvider3`() {
        expect that threeParams("test", 42, true).bar() isEqualTo ""
        
        every { threeParams("test", 42, true).bar() } returns "mocked with three params"
        
        expect that threeParams("test", 42, true).bar() isEqualTo "mocked with three params"
    }
    
    @Test
    fun `mocks NewProvider0`() {
        expect that (newOne() === newOne()) _is true
        expect that newOne().bar() isEqualTo ""

        every { newOne().bar() } returns "mocked new instance"

        expect that newOne().bar() isEqualTo "mocked new instance"
        expect that newOne().bar() isEqualTo "mocked new instance"
    }
    
    @Test
    fun `mocks NewProvider1`() {
        expect that newParameterized("test").bar() isEqualTo ""
        
        every { newParameterized("test").bar() } returns "mocked new with param"

        expect that newParameterized("test").bar() isEqualTo "mocked new with param"
    }
    
    @Test
    fun `mocks NewProvider2`() {
        expect that newTwoParams("test", 42).bar() isEqualTo ""
        
        every { newTwoParams("test", 42).bar() } returns "mocked new with two params"

        expect that newTwoParams("test", 42).bar() isEqualTo "mocked new with two params"
    }
    
    @Test
    fun `mocks NewProvider3`() {
        expect that newThreeParams("test", 42, true).bar() isEqualTo ""
        
        every { newThreeParams("test", 42, true).bar() } returns "mocked new with three params"

        expect that newThreeParams("test", 42, true).bar() isEqualTo "mocked new with three params"
    }
}