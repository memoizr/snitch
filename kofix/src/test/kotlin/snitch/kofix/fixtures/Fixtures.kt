package snitch.kofix.fixtures

import java.math.BigDecimal

interface SealedInterface {
    val type get() = this::class.simpleName
}
sealed class SealedClass {
    data class One(val x: String) : SealedClass()
    data class Two(val x: String) : SealedClass()
}

data class ProblematicConstructorClass(val x: String, val y: SimpleClass) {
    init {
        throw Exception("gotcha")
    }
}

interface InterfaceWithNoImplementations {
    val simpleClass: SimpleClass
    fun listOfObjects(): List<SimpleClass>
    fun listOfObjects(string: String): List<String>
    fun string(): String
    fun arrayOfObjects(): Array<SimpleClass>
}

interface AnInterface
object AnObject
data class AClassWithObject(val anObject: AnObject)
//data class ArrayClass(val x: Array<List<Int?>>)
data class AnImplementation(val x: String) : AnInterface

data class AClassWithInterface(val inter: AnInterface, val foo: Interface1)
data class SimpleClass(val name: String)
data class OtherSimpleClass(val otherName: String)
data class SimpleCompoundClass(val simpleClass: SimpleClass, val otherSimpleClass: OtherSimpleClass)
data class NullableClass(val name: String, val nullable: String?)
data class RecursiveClass(val sample: SimpleClass, val nullableClass: NullableClass)
data class ClassWithEnum(val enum: TheEnum)
data class ClassWithBigDecimal(val bigDecimal: BigDecimal)
data class ClassWithList(val list: List<String>)
data class ClassWithListAndParam(val list: List<String>, val param: String)
data class ClassWithMutableList(val list: MutableList<String>)
data class CyclicClass(val cycles: List<CyclicClass>, val cycle: CyclicClass)
data class ClassWithArrays(
        val int: IntArray,
        val intA: Array<Int>,
        val short: ShortArray,
        val shortA: Array<Short>,
        val long: LongArray,
        val longA: Array<Long>,
        val float: FloatArray,
        val floatA: Array<Float>,
        val double: DoubleArray,
        val doubleA: Array<Double>,
        val boolean: BooleanArray,
        val booleanA: Array<Boolean>,
        val byte: ByteArray,
        val byteA: Array<Byte>,
        val char: CharArray,
        val charA: Array<Char>,
        val objA: Array<SimpleClass>
)

data class ClassWithPrimitives(
        val int: Int,
        val short: Short,
        val long: Long,
        val float: Float,
        val double: Double,
        val boolean: Boolean,
        val byte: Byte,
        val char: Char
)

enum class TheEnum {
    One, Two, Three, Four
}

interface Interface1
data class Impl1Interface1(val x: String) : Interface1
data class Impl2Interface1(val x: String) : Interface1
data class Impl3Interface1(val x: String) : Interface1
data class Impl4Interface1(val x: String) : Interface1
data class Impl5Interface1(val x: String) : Interface1
data class Impl6Interface1(val x: String) : Interface1
data class Impl7Interface1(val x: String) : Interface1
data class Impl8Interface1(val x: String) : Interface1
data class Impl9Interface1(val x: String) : Interface1
data class Impl10Interface1(val x: String) : Interface1
data class Impl11Interface1(val x: String) : Interface1
data class Impl12Interface1(val x: String) : Interface1
data class Impl13Interface1(val x: String) : Interface1
data class Impl14Interface1(val x: String) : Interface1
data class Impl15Interface1(val x: String) : Interface1
data class Impl16Interface1(val x: String) : Interface1
data class Impl17Interface1(val x: String) : Interface1
data class Impl18Interface1(val x: String) : Interface1
data class Impl19Interface1(val x: String) : Interface1

interface Interface2
data class Impl1Interface2(val x: String) : Interface2
data class Impl2Interface2(val x: String) : Interface2
data class Impl3Interface2(val x: String) : Interface2
data class Impl4Interface2(val x: String) : Interface2
data class Impl5Interface2(val x: String) : Interface2
data class Impl6Interface2(val x: String) : Interface2
data class Impl7Interface2(val x: String) : Interface2
data class Impl8Interface2(val x: String) : Interface2
data class Impl9Interface2(val x: String) : Interface2
data class Impl10Interface2(val x: String) : Interface2
data class Impl11Interface2(val x: String) : Interface2
data class Impl12Interface2(val x: String) : Interface2
data class Impl13Interface2(val x: String) : Interface2
data class Impl14Interface2(val x: String) : Interface2
data class Impl15Interface2(val x: String) : Interface2
data class Impl16Interface2(val x: String) : Interface2
data class Impl17Interface2(val x: String) : Interface2
data class Impl18Interface2(val x: String) : Interface2
data class Impl19Interface2(val x: String) : Interface2
data class Impl20Interface2(val x: String) : Interface2

interface Interface3
data class Impl1Interface3(val x: String) : Interface3
data class Impl2Interface3(val x: String) : Interface3
data class Impl3Interface3(val x: String) : Interface3
data class Impl4Interface3(val x: String) : Interface3
data class Impl5Interface3(val x: String) : Interface3
data class Impl6Interface3(val x: String) : Interface3
data class Impl7Interface3(val x: String) : Interface3
data class Impl8Interface3(val x: String) : Interface3
data class Impl9Interface3(val x: String) : Interface3
data class Impl10Interface3(val x: String) : Interface3
data class Impl11Interface3(val x: String) : Interface3
data class Impl12Interface3(val x: String) : Interface3
data class Impl13Interface3(val x: String) : Interface3
data class Impl14Interface3(val x: String) : Interface3
data class Impl15Interface3(val x: String) : Interface3
data class Impl16Interface3(val x: String) : Interface3
data class Impl17Interface3(val x: String) : Interface3
data class Impl18Interface3(val x: String) : Interface3
data class Impl19Interface3(val x: String) : Interface3

interface Interface4
data class Impl1Interface4(val x: String) : Interface4
data class Impl2Interface4(val x: String) : Interface4
data class Impl3Interface4(val x: String) : Interface4
data class Impl4Interface4(val x: String) : Interface4
data class Impl5Interface4(val x: String) : Interface4
data class Impl6Interface4(val x: String) : Interface4
data class Impl7Interface4(val x: String) : Interface4
data class Impl8Interface4(val x: String) : Interface4
data class Impl9Interface4(val x: String) : Interface4
data class Impl10Interface4(val x: String) : Interface4
data class Impl11Interface4(val x: String) : Interface4
data class Impl12Interface4(val x: String) : Interface4
data class Impl13Interface4(val x: String) : Interface4
data class Impl14Interface4(val x: String) : Interface4
data class Impl15Interface4(val x: String) : Interface4
data class Impl16Interface4(val x: String) : Interface4
data class Impl17Interface4(val x: String) : Interface4
data class Impl18Interface4(val x: String) : Interface4
data class Impl19Interface4(val x: String) : Interface4

interface Interface5
data class Impl1Interface5(val x: String) : Interface5
data class Impl2Interface5(val x: String) : Interface5
data class Impl3Interface5(val x: String) : Interface5
data class Impl4Interface5(val x: String) : Interface5
data class Impl5Interface5(val x: String) : Interface5
data class Impl6Interface5(val x: String) : Interface5
data class Impl7Interface5(val x: String) : Interface5
data class Impl8Interface5(val x: String) : Interface5
data class Impl9Interface5(val x: String) : Interface5
data class Impl10Interface5(val x: String) : Interface5
data class Impl11Interface5(val x: String) : Interface5
data class Impl12Interface5(val x: String) : Interface5
data class Impl13Interface5(val x: String) : Interface5
data class Impl14Interface5(val x: String) : Interface5
data class Impl15Interface5(val x: String) : Interface5
data class Impl16Interface5(val x: String) : Interface5
data class Impl17Interface5(val x: String) : Interface5
data class Impl18Interface5(val x: String) : Interface5
data class Impl19Interface5(val x: String) : Interface5

interface Interface6
data class Impl1Interface6(val x: String) : Interface6
data class Impl2Interface6(val x: String) : Interface6
data class Impl3Interface6(val x: String) : Interface6
data class Impl4Interface6(val x: String) : Interface6
data class Impl5Interface6(val x: String) : Interface6
data class Impl6Interface6(val x: String) : Interface6
data class Impl7Interface6(val x: String) : Interface6
data class Impl8Interface6(val x: String) : Interface6
data class Impl9Interface6(val x: String) : Interface6
data class Impl10Interface6(val x: String) : Interface6
data class Impl11Interface6(val x: String) : Interface6
data class Impl12Interface6(val x: String) : Interface6
data class Impl13Interface6(val x: String) : Interface6
data class Impl14Interface6(val x: String) : Interface6
data class Impl15Interface6(val x: String) : Interface6
data class Impl16Interface6(val x: String) : Interface6
data class Impl17Interface6(val x: String) : Interface6
data class Impl18Interface6(val x: String) : Interface6
data class Impl19Interface6(val x: String) : Interface6

interface Interface7
data class Impl1Interface7(val x: String) : Interface7
data class Impl2Interface7(val x: String) : Interface7
data class Impl3Interface7(val x: String) : Interface7
data class Impl4Interface7(val x: String) : Interface7
data class Impl5Interface7(val x: String) : Interface7
data class Impl6Interface7(val x: String) : Interface7
data class Impl7Interface7(val x: String) : Interface7
data class Impl8Interface7(val x: String) : Interface7
data class Impl9Interface7(val x: String) : Interface7
data class Impl10Interface7(val x: String) : Interface7
data class Impl11Interface7(val x: String) : Interface7
data class Impl12Interface7(val x: String) : Interface7
data class Impl13Interface7(val x: String) : Interface7
data class Impl14Interface7(val x: String) : Interface7
data class Impl15Interface7(val x: String) : Interface7
data class Impl16Interface7(val x: String) : Interface7
data class Impl17Interface7(val x: String) : Interface7
data class Impl18Interface7(val x: String) : Interface7
data class Impl19Interface7(val x: String) : Interface7

interface Interface8
data class Impl1Interface8(val x: String) : Interface8
data class Impl2Interface8(val x: String) : Interface8
data class Impl3Interface8(val x: String) : Interface8
data class Impl4Interface8(val x: String) : Interface8
data class Impl5Interface8(val x: String) : Interface8
data class Impl6Interface8(val x: String) : Interface8
data class Impl7Interface8(val x: String) : Interface8
data class Impl8Interface8(val x: String) : Interface8
data class Impl9Interface8(val x: String) : Interface8
data class Impl10Interface8(val x: String) : Interface8
data class Impl11Interface8(val x: String) : Interface8
data class Impl12Interface8(val x: String) : Interface8
data class Impl13Interface8(val x: String) : Interface8
data class Impl14Interface8(val x: String) : Interface8
data class Impl15Interface8(val x: String) : Interface8
data class Impl16Interface8(val x: String) : Interface8
data class Impl17Interface8(val x: String) : Interface8
data class Impl18Interface8(val x: String) : Interface8
data class Impl19Interface8(val x: String) : Interface8

interface Interface9
data class Impl1Interface9(val x: String) : Interface9
data class Impl2Interface9(val x: String) : Interface9
data class Impl3Interface9(val x: String) : Interface9
data class Impl4Interface9(val x: String) : Interface9
data class Impl5Interface9(val x: String) : Interface9
data class Impl6Interface9(val x: String) : Interface9
data class Impl7Interface9(val x: String) : Interface9
data class Impl8Interface9(val x: String) : Interface9
data class Impl9Interface9(val x: String) : Interface9
data class Impl10Interface9(val x: String) : Interface9
data class Impl11Interface9(val x: String) : Interface9
data class Impl12Interface9(val x: String) : Interface9
data class Impl13Interface9(val x: String) : Interface9
data class Impl14Interface9(val x: String) : Interface9
data class Impl15Interface9(val x: String) : Interface9
data class Impl16Interface9(val x: String) : Interface9
data class Impl17Interface9(val x: String) : Interface9
data class Impl18Interface9(val x: String) : Interface9
data class Impl19Interface9(val x: String) : Interface9

interface Interface10
data class Impl1Interface10(val x: String) : Interface10
data class Impl2Interface10(val x: String) : Interface10
data class Impl3Interface10(val x: String) : Interface10
data class Impl4Interface10(val x: String) : Interface10
data class Impl5Interface10(val x: String) : Interface10
data class Impl6Interface10(val x: String) : Interface10
data class Impl7Interface10(val x: String) : Interface10
data class Impl8Interface10(val x: String) : Interface10
data class Impl9Interface10(val x: String) : Interface10
data class Impl10Interface10(val x: String) : Interface10
data class Impl11Interface10(val x: String) : Interface10
data class Impl12Interface10(val x: String) : Interface10
data class Impl13Interface10(val x: String) : Interface10
data class Impl14Interface10(val x: String) : Interface10
data class Impl15Interface10(val x: String) : Interface10
data class Impl16Interface10(val x: String) : Interface10
data class Impl17Interface10(val x: String) : Interface10
data class Impl18Interface10(val x: String) : Interface10
data class Impl19Interface10(val x: String) : Interface10


interface Interface11
data class Impl1Interface11(val x: String) : Interface11
data class Impl2Interface11(val x: String) : Interface11
data class Impl3Interface11(val x: String) : Interface11
data class Impl4Interface11(val x: String) : Interface11
data class Impl5Interface11(val x: String) : Interface11
data class Impl6Interface11(val x: String) : Interface11
data class Impl7Interface11(val x: String) : Interface11
data class Impl8Interface11(val x: String) : Interface11
data class Impl9Interface11(val x: String) : Interface11
data class Impl10Interface11(val x: String) : Interface11
data class Impl11Interface11(val x: String) : Interface11
data class Impl12Interface11(val x: String) : Interface11
data class Impl13Interface11(val x: String) : Interface11
data class Impl14Interface11(val x: String) : Interface11
data class Impl15Interface11(val x: String) : Interface11
data class Impl16Interface11(val x: String) : Interface11
data class Impl17Interface11(val x: String) : Interface11
data class Impl18Interface11(val x: String) : Interface11
data class Impl19Interface11(val x: String) : Interface11

interface Interface12
data class Impl1Interface12(val x: String) : Interface12
data class Impl2Interface12(val x: String) : Interface12
data class Impl3Interface12(val x: String) : Interface12
data class Impl4Interface12(val x: String) : Interface12
data class Impl5Interface12(val x: String) : Interface12
data class Impl6Interface12(val x: String) : Interface12
data class Impl7Interface12(val x: String) : Interface12
data class Impl8Interface12(val x: String) : Interface12
data class Impl9Interface12(val x: String) : Interface12
data class Impl10Interface12(val x: String) : Interface12
data class Impl11Interface12(val x: String) : Interface12
data class Impl12Interface12(val x: String) : Interface12
data class Impl13Interface12(val x: String) : Interface12
data class Impl14Interface12(val x: String) : Interface12
data class Impl15Interface12(val x: String) : Interface12
data class Impl16Interface12(val x: String) : Interface12
data class Impl17Interface12(val x: String) : Interface12
data class Impl18Interface12(val x: String) : Interface12
data class Impl19Interface12(val x: String) : Interface12

interface Interface13
data class Impl1Interface13(val x: String) : Interface13
data class Impl2Interface13(val x: String) : Interface13
data class Impl3Interface13(val x: String) : Interface13
data class Impl4Interface13(val x: String) : Interface13
data class Impl5Interface13(val x: String) : Interface13
data class Impl6Interface13(val x: String) : Interface13
data class Impl7Interface13(val x: String) : Interface13
data class Impl8Interface13(val x: String) : Interface13
data class Impl9Interface13(val x: String) : Interface13
data class Impl10Interface13(val x: String) : Interface13
data class Impl11Interface13(val x: String) : Interface13
data class Impl12Interface13(val x: String) : Interface13
data class Impl13Interface13(val x: String) : Interface13
data class Impl14Interface13(val x: String) : Interface13
data class Impl15Interface13(val x: String) : Interface13
data class Impl16Interface13(val x: String) : Interface13
data class Impl17Interface13(val x: String) : Interface13
data class Impl18Interface13(val x: String) : Interface13
data class Impl19Interface13(val x: String) : Interface13

interface Interface14
data class Impl1Interface14(val x: String) : Interface14
data class Impl2Interface14(val x: String) : Interface14
data class Impl3Interface14(val x: String) : Interface14
data class Impl4Interface14(val x: String) : Interface14
data class Impl5Interface14(val x: String) : Interface14
data class Impl6Interface14(val x: String) : Interface14
data class Impl7Interface14(val x: String) : Interface14
data class Impl8Interface14(val x: String) : Interface14
data class Impl9Interface14(val x: String) : Interface14
data class Impl10Interface14(val x: String) : Interface14
data class Impl11Interface14(val x: String) : Interface14
data class Impl12Interface14(val x: String) : Interface14
data class Impl13Interface14(val x: String) : Interface14
data class Impl14Interface14(val x: String) : Interface14
data class Impl15Interface14(val x: String) : Interface14
data class Impl16Interface14(val x: String) : Interface14
data class Impl17Interface14(val x: String) : Interface14
data class Impl18Interface14(val x: String) : Interface14
data class Impl19Interface14(val x: String) : Interface14

interface Interface15
data class Impl1Interface15(val x: String) : Interface15
data class Impl2Interface15(val x: String) : Interface15
data class Impl3Interface15(val x: String) : Interface15
data class Impl4Interface15(val x: String) : Interface15
data class Impl5Interface15(val x: String) : Interface15
data class Impl6Interface15(val x: String) : Interface15
data class Impl7Interface15(val x: String) : Interface15
data class Impl8Interface15(val x: String) : Interface15
data class Impl9Interface15(val x: String) : Interface15
data class Impl10Interface15(val x: String) : Interface15
data class Impl11Interface15(val x: String) : Interface15
data class Impl12Interface15(val x: String) : Interface15
data class Impl13Interface15(val x: String) : Interface15
data class Impl14Interface15(val x: String) : Interface15
data class Impl15Interface15(val x: String) : Interface15
data class Impl16Interface15(val x: String) : Interface15
data class Impl17Interface15(val x: String) : Interface15
data class Impl18Interface15(val x: String) : Interface15
data class Impl19Interface15(val x: String) : Interface15

data class Fooed(
    val val1: List<Interface1>,
    val val2: List<Interface2>,
    val val3: List<Interface3>,
    val val4: List<Interface4>,
    val val5: List<Interface5>,
    val val6: List<Interface6>,
    val val7: List<Interface7>,
    val val8: List<Interface8>,
    val val9: List<Interface9>,
    val val10: List<Interface10>,
    val val11: List<Interface11>,
    val val12: List<Interface12>,
    val val13: List<Interface13>,
    val val14: List<Interface14>,
    val val15: List<Interface15>,
    val sealed: List<SealedClass>
)
