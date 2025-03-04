package snitch.shank

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import shouldBeEqualTo
import snitch.shank.ParameterNewModule.fiveParamNew
import snitch.shank.ParameterNewModule.fourParamNew
import snitch.shank.ParameterNewModule.noParamNew
import snitch.shank.ParameterNewModule.oneParamNew
import snitch.shank.ParameterNewModule.threeParamNew
import snitch.shank.ParameterNewModule.twoParamNew

private object ParameterNewModule : ShankModule {
    val noParamNew = new { -> ParamData() }
    val oneParamNew = new { a: Int -> ParamData(a) }
    val twoParamNew = new { a: Int, b: Int -> ParamData(a, b) }
    val threeParamNew = new { a: Int, b: Int, c: Int -> ParamData(a, b, c) }
    val fourParamNew = new { a: Int, b: Int, c: Int, d: Int -> ParamData(a, b, c, d) }
    val fiveParamNew = new { a: Int, b: Int, c: Int, d: Int, e: Int -> ParamData(a, b, c, d, e) }
}

class NewTests {

    @BeforeEach
    fun setUp() {
        resetShank()
    }

    @Test
    fun `should create new instances with no parameters`() {
        noParamNew.override(null)
        noParamNew() shouldBeEqualTo ParamData()
    }
    
    @Test
    fun `should create new instances with one parameter`() {
        oneParamNew.override(null)
        oneParamNew(1) shouldBeEqualTo ParamData(1)
    }
    
    @Test
    fun `should create new instances with two parameters`() {
        twoParamNew.override(null)
        twoParamNew(1, 2) shouldBeEqualTo ParamData(1, 2)
    }
    
    @Test
    fun `should create new instances with three parameters`() {
        threeParamNew.override(null)
        threeParamNew(1, 2, 3) shouldBeEqualTo ParamData(1, 2, 3)
    }
    
    @Test
    fun `should create new instances with four parameters`() {
        fourParamNew.override(null)
        fourParamNew(1, 2, 3, 4) shouldBeEqualTo ParamData(1, 2, 3, 4)
    }
    
    @Test
    fun `should create new instances with five parameters`() {
        fiveParamNew.override(null)
        fiveParamNew(1, 2, 3, 4, 5) shouldBeEqualTo ParamData(1, 2, 3, 4, 5)
    }

    @Test
    fun `should allow overriding factory implementation with no parameters`() {
        noParamNew() shouldBeEqualTo ParamData(null)
        noParamNew.override { -> ParamData(2) }
        noParamNew() shouldBeEqualTo ParamData(2)
    }
    
    @Test
    fun `should allow overriding factory implementation with one parameter`() {
        oneParamNew(1) shouldBeEqualTo ParamData(1)
        oneParamNew.override { a: Int -> ParamData(a * 2) }
        oneParamNew(1) shouldBeEqualTo ParamData(2)
    }
    
    @Test
    fun `should allow overriding factory implementation with two parameters`() {
        twoParamNew(1, 2) shouldBeEqualTo ParamData(1, 2)
        twoParamNew.override { a: Int, b: Int -> ParamData(a * 2, b * 2) }
        twoParamNew(1, 2) shouldBeEqualTo ParamData(2, 4)
    }
    
    @Test
    fun `should allow overriding factory implementation with three parameters`() {
        threeParamNew(1, 2, 3) shouldBeEqualTo ParamData(1, 2, 3)
        threeParamNew.override { a: Int, b: Int, c: Int -> ParamData(a * 2, b * 2, c * 2) }
        threeParamNew(1, 2, 3) shouldBeEqualTo ParamData(2, 4, 6)
    }
    
    @Test
    fun `should allow overriding factory implementation with four parameters`() {
        fourParamNew(1, 2, 3, 4) shouldBeEqualTo ParamData(1, 2, 3, 4)
        fourParamNew.override { a: Int, b: Int, c: Int, d: Int ->
            ParamData(
                a * 2,
                b * 2,
                c * 2,
                d * 2
            )
        }
        fourParamNew(1, 2, 3, 4) shouldBeEqualTo ParamData(2, 4, 6, 8)
    }
    
    @Test
    fun `should allow overriding factory implementation with five parameters`() {
        fiveParamNew(1, 2, 3, 4, 5) shouldBeEqualTo ParamData(1, 2, 3, 4, 5)
        fiveParamNew.override { a: Int, b: Int, c: Int, d: Int, e: Int ->
            ParamData(
                a * 2,
                b * 2,
                c * 2,
                d * 2,
                e * 2
            )
        }
        fiveParamNew(1, 2, 3, 4, 5) shouldBeEqualTo ParamData(2, 4, 6, 8, 10)
    }
}
