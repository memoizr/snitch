package snitch.shank

import snitch.shank.MyModule.listOfInts
import snitch.shank.MyModule.listOfStrings
import snitch.shank.MyModule.newFoo
import snitch.shank.MyModule.otherScopeSingleton
import snitch.shank.MyModule.scopedSingleton
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import shouldBeEqualTo
import shouldBeInstanceOf
import shouldNotBeEqualTo

object MyModule : ShankModule {
    val newFoo = new { -> MyClass("my value") }
    val listOfStrings = new { -> ArrayList<String>().also { it.add("one") } }
    val listOfInts = new { -> ArrayList<Int>().also { it.add(1) } }

    val scopedSingleton = scoped { -> OtherClass() }
    val otherScopeSingleton = scoped { -> MyClass("value") }
//    val singleton = singleton { -> OtherClass() }

    val nullable = new { -> if ("" == "") MyClass("") else null }
    val nullable1 = new { s: String?, b: String -> if (s == "") MyClass("") else null }
}

class ShankTest : Scoped {
    override val scope = Scope("myscope")

    @BeforeEach
    fun setUp() {
        resetShank()
    }

    @Test
    fun `provides new value`() {
        newFoo().value shouldBeEqualTo "my value"
        newFoo() shouldNotBeEqualTo newFoo()
    }

    @Test
    fun `supports generics`() {
        listOfStrings()[0] shouldBeEqualTo "one"
        listOfInts()[0] shouldBeEqualTo 1
    }

    @Test
    fun `caches values for a specific scope`() {
        val scope = Scope("scope")
        scopedSingleton(scope) shouldBeEqualTo scopedSingleton(scope)
        otherScopeSingleton(scope) shouldBeInstanceOf MyClass::class
    }

    @Test
    fun `clears a specific scope`() {
        val scope = Scope("scope")
        val instance1 = scopedSingleton(scope)
        val instance2 = scopedSingleton(scope)
        instance1 shouldBeEqualTo instance2
        scope.clear()
        val instance3 = scopedSingleton(scope)
        instance1 shouldNotBeEqualTo instance3
        instance2 shouldNotBeEqualTo instance3

        val instance4 = scopedSingleton(scope)
        instance3 shouldBeEqualTo instance4
    }

    @Test
    fun `supports global scope`() {
//        singleton() shouldBeEqualTo singleton()
    }

    @Test
    fun `automatically injects scope for scoped class`() {
        scopedSingleton() shouldBeEqualTo scopedSingleton()
    }
}

class OtherClass
class MyClass(val value: String)

data class ParamData(
    val a: Int? = null,
    val b: Int? = null,
    val c: Int? = null,
    val d: Int? = null,
    val e: Int? = null
)
