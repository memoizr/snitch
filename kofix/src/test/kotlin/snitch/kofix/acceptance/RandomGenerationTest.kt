@file:Suppress("UNCHECKED_CAST")

package snitch.kofix.acceptance

import com.memoizr.assertk.expect
import com.memoizr.assertk.isEqualTo
import com.memoizr.assertk.isInstance
import com.memoizr.assertk.notNull
import com.memoizr.assertk.of
import snitch.kofix.CreationException
import snitch.kofix.JavaClassWithList
import snitch.kofix.Seed
import snitch.kofix.aRandom
import snitch.kofix.aRandomListOf
import snitch.kofix.customize
import snitch.kofix.fixtures.AClassWithInterface
import snitch.kofix.fixtures.AClassWithObject
import snitch.kofix.fixtures.AnInterface
import snitch.kofix.fixtures.AnObject
import snitch.kofix.fixtures.ClassWithArrays
import snitch.kofix.fixtures.ClassWithBigDecimal
import snitch.kofix.fixtures.ClassWithEnum
import snitch.kofix.fixtures.ClassWithList
import snitch.kofix.fixtures.ClassWithMutableList
import snitch.kofix.fixtures.ClassWithPrimitives
import snitch.kofix.fixtures.CyclicClass
import snitch.kofix.fixtures.Fooed
import snitch.kofix.fixtures.InterfaceWithNoImplementations
import snitch.kofix.fixtures.NullableClass
import snitch.kofix.fixtures.ProblematicConstructorClass
import snitch.kofix.fixtures.RecursiveClass
import snitch.kofix.fixtures.SealedClass
import snitch.kofix.fixtures.SimpleClass
import snitch.kofix.fixtures.SimpleCompoundClass
import snitch.kofix.fixtures.TheEnum
import org.junit.Before
import org.junit.Test
import java.io.File
import java.math.BigDecimal
import java.time.Instant
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

@Suppress("MemberVisibilityCanBePrivate")
class RandomGenerationTest {
    val aSimpleClass by aRandom<SimpleClass>()
    val anotherSimpleClass by aRandom<SimpleClass>()
    val aNullableClass by aRandom<NullableClass>()
    val aRecursiveClass by aRandom<RecursiveClass>()
    val anotherRecursiveClass by aRandom<RecursiveClass>()
    val aClassWithEnum by aRandom<ClassWithEnum>()
    val aClassWithBigDecimal by aRandom<ClassWithBigDecimal>()
    val aSimpleCompoundClass by aRandom<SimpleCompoundClass>()
    val aClassWithList by aRandom<ClassWithList>()
    val aClassWithMutableList by aRandom<ClassWithMutableList>()
    val aJavaClassWithList by aRandom<JavaClassWithList>()
    val aClassWithPrimitives by aRandom<ClassWithPrimitives>()
    val aDate by aRandom<Date>()
    val anInstant by aRandom<Instant>()
    val aUUID by aRandom<UUID>()

    @Before
    fun setUp() {
        Seed.testing = true
    }

    @Test
    fun `creates an arbitrary data class`() {
        expect that aSimpleClass isInstance of<SimpleClass>()
        expect that aSimpleClass.name.length isBetween 1..20
        expect that aSimpleClass.name isEqualTo aSimpleClass.name
    }

    @Test
    fun `creates a date`() {
        expect that aDate isInstance of<Date>()
    }

    @Test
    fun `creates an instant`() {
        expect that anInstant isInstance of<Instant>()
    }

    @Test
    fun `creates an UUID`() {
        expect that aUUID isInstance of<UUID>()
    }

    @Test
    fun `returns different results for different seeds`() {
        (1..100).map {
            Seed.seed = Random().nextLong()
            println(aSimpleClass.name)
            aSimpleClass.name
        }.toSet().size isEqualTo 100
    }

    @Test
    fun `returns null or not null randomly for nullable values`() {
        val groupedValues: List<Pair<Boolean, Int>> = (1..1000).map {
            Seed.seed = Random().nextLong()
            Pair(aNullableClass.nullable == null, 1)
        }.groupBy { it.first }.map { (k, v) -> Pair(k, v.count()) }

        expect that groupedValues[0].second isCloseTo groupedValues[1].second withinPercentage 15
    }

    @Test
    fun `it generates different values for each parameter`() {
        expect that aSimpleCompoundClass.simpleClass.name isEqualTo aSimpleCompoundClass.simpleClass.name
        expect that aSimpleCompoundClass.simpleClass.name isNotEqualTo aSimpleCompoundClass.otherSimpleClass.otherName
    }

    @Test
    fun `it creates objects recursively`() {
        expect that aRecursiveClass isInstance of<RecursiveClass>()
        expect that aRecursiveClass.sample isEqualTo aRecursiveClass.sample
    }

    @Test
    fun `it creates different objects for different property values`() {
        expect that aSimpleClass isInstance of<SimpleClass>()
        expect that anotherSimpleClass isInstance of<SimpleClass>()
        expect that aSimpleClass isNotEqualTo anotherSimpleClass
        expect that anotherSimpleClass.name isEqualTo anotherSimpleClass.name
        expect that anotherRecursiveClass.sample isEqualTo anotherRecursiveClass.sample
    }

    @Test
    fun `it generates random enums`() {
        val enums = (1..1000).map {
            Seed.seed = Random().nextLong()
            aClassWithEnum.enum
        }.groupBy { a -> a }

        enums.forEach { _, u ->
            expect that u.size isCloseTo 250 withinPercentage 15
        }
        expect that enums.size isEqualTo 4
        expect that aClassWithEnum.enum isInstance of<TheEnum>()
    }


    @Test
    fun `it allows to customize object creation`() {
        customize<BigDecimal>() { BigDecimal(a<Int>()) }
        expect that aClassWithBigDecimal isInstance of<ClassWithBigDecimal>()
    }

    @Test
    fun `it generates lists`() {
        expect that aClassWithList.list isEqualTo aClassWithList.list
        expect that aClassWithMutableList.list.plus("hey") contains mutableListOf("hey")
        expect that aJavaClassWithList isEqualTo aJavaClassWithList
    }

    val listOfMinSize by aRandomListOf<SimpleClass>(minSize = 3)

    @Test
    fun `it generates list of min size`() {
        (1..1000).map {
            Seed.seed = Random().nextLong()
            expect that listOfMinSize.size isGreaterThanOrEqualTo 3
        }
    }

    val listOfMaxSize by aRandomListOf<SimpleClass>(maxSize = 4)

    @Test
    fun `it generates list of max size`() {
        (1..1000).map {
            Seed.seed = Random().nextLong()
            expect that listOfMaxSize.size isLessThanOrEqualTo 4
        }
    }

    val listOfMinMaxSize by aRandomListOf<SimpleClass>(minSize = 2, maxSize = 3)

    @Test
    fun `it generates list of min max size`() {
        (1..1000).map {
            Seed.seed = Random().nextLong()
            expect that listOfMinMaxSize.size isBetween 2..3
        }
    }

    @Test
    fun `covers all the primitives`() {
        expect that aClassWithPrimitives isEqualTo aClassWithPrimitives
    }

    val aSimpleClassCustomized by aRandom<SimpleClass> { copy(name = "foo") }

    @Test
    fun `objects can be customized`() {
        expect that aSimpleClassCustomized.name isEqualTo "foo"
    }

    val aCyclicClass by aRandom<CyclicClass>()

    @Test
    fun `does not allow for recursive classes`() {
        expect thatThrownBy { aCyclicClass } hasMessageContaining "cyclic"
    }

    val aRandomList by aRandomListOf<SimpleClass>()
    val aRandomListOfList by aRandomListOf<List<List<SimpleClass>>>()
    val aRandomListSize10 by aRandomListOf<SimpleClass>(size = 10)

    @Test
    fun `creates a random list`() {
        expect that aRandomListOfList.size isGreaterThan 0
        expect that aRandomList.size isGreaterThan 0
        expect that aRandomListSize10.size isEqualTo 10
    }

    val aCustomList by aRandomListOf<SimpleClass> { map { it.copy(name = "foo") } }

    @Test
    fun `lists can be customized`() {
        expect that aCustomList.all { it.name == "foo" } _is true
    }

    val aClassWithInterface by aRandom<AClassWithInterface>()

    @Test
    fun `it copes with interfaces in same package`() {
        expect that aClassWithInterface.inter isInstance of<AnInterface>()
    }

    val aClassWithObject by aRandom<AClassWithObject>()

    @Test
    fun `it copes with objects`() {
        expect that aClassWithObject.anObject isEqualTo AnObject
    }

    val aPair by aRandom<Pair<String, Int>>()

    @Test
    fun `creates instances of generic classes`() {
        expect that aPair.first isInstance of<String>()
        expect that aPair.second isInstance of<Int>()
    }

    val aSet by aRandom<Set<Set<String>>>()

    @Test
    fun `creates a set`() {
        expect that aSet.size isGreaterThan 0
    }

    val aMap by aRandom<Map<String, SimpleClass>>()

    @Test
    fun `create a map`() {
        expect that aMap.size isGreaterThan 0
        expect that aMap[aMap.keys.first()] isInstance of<SimpleClass>()
    }

    val aSealedClass by aRandom<SealedClass>()

    @Test
    fun `creates sealed classes`() {
        expect that aSealedClass isInstance of<SealedClass>()
    }

    val aFooBar by aRandom<Fooed>()

    @Test
    fun `goes fast with interfaces`() {
        expect that aFooBar _is notNull
    }

    val aFile by aRandom<File>()

    @Test
    fun `creates File`() {
        expect that aFile _is notNull
    }

    val anArrayClass by aRandom<ClassWithArrays>()

    @Test
    fun `it works with arrays`() {
        expect that anArrayClass.boolean[0] isInstance of<Boolean>()
    }

    interface InterfaceWithGenericTypes<A, B> {
        fun getLong(): Long
        fun getB(): B
        fun getA(): List<A>
        fun getSimpleClass(): SimpleClass
    }

    val genericInterface by aRandom<InterfaceWithGenericTypes<List<String>, Int>>()

    @Test
    fun `it instantiates a random generic interface`() {
        expect that genericInterface.getA().first().first() isInstance of<String>()
        expect that genericInterface.getB() isInstance of<Int>()
        expect that genericInterface.getLong() isInstance of<Long>()
        expect that genericInterface.getSimpleClass() isInstance of<SimpleClass>()
    }


    abstract class AnAbstractClass {
        abstract fun getString(): String
        fun getValue() = "value"
    }

    val genericAbstractClass by aRandom<AnAbstractClass>()

    @Test
    fun `it instantiate an abstract class`() {
        expect that genericAbstractClass.getString() isInstance of<String>()
        expect that genericAbstractClass.getValue() isEqualToIgnoringCase "value"
    }

    val interfaceImpl by aRandom<InterfaceWithNoImplementations>()

    @Test
    fun `it works with interfaces with no implementations`() {
        expect that interfaceImpl.listOfObjects().first() isInstance of<SimpleClass>()
        expect that interfaceImpl.listOfObjects("").first() isInstance of<String>()
        expect that interfaceImpl.arrayOfObjects().first() isInstance of<SimpleClass>()
        expect that interfaceImpl.string() isInstance of<String>()
        expect that interfaceImpl.simpleClass isInstance of<SimpleClass>()
        expect that interfaceImpl isEqualTo interfaceImpl
    }

    val problematicClass by aRandom<ProblematicConstructorClass>()

    @Test
    fun `throws meaningful exception when instantiation fails`() {
        expect thatThrownBy { problematicClass } hasMessageContaining
                "Something went wrong" hasMessageContaining
                "with values" hasMessageContaining
                "y=" hasMessageContaining
                "x=" hasCauseExactlyInstanceOf
                Exception::class.java isInstance of<CreationException>()
    }

    val listCacheCounter = AtomicInteger()

    val aListOfStrings by aRandomListOf<String>(30) {
        listCacheCounter.incrementAndGet()
        this
    }

    @Test
    fun `list creations are cached`() {
        (1..30).forEach {
            expect that aListOfStrings hasSize 30
            expect that listCacheCounter.get() isEqualTo 1
        }
    }

    data class Username(val value: String)
    data class Id(val value: String)
    data class User(val username: Username, val id: Id)

    val user by aRandom<User>()

    val users by aRandomListOf<User>()

    @Test
    fun `includes more descriptive strings`() {
        customize { Username("${property.name}_username_${a<String>()}_${a<String>()}") }
        customize { Id("${property.name}_id_${a<String>()}") }

        expect that users.map { it.id.value }.toSet().size isEqualTo users.size
        expect that user.username.value contains "user_username"
        expect that user.id.value contains "user_id"
    }
}
