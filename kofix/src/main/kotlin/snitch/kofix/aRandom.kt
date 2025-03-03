package snitch.kofix

import snitch.kofix.CreationLogic.aList
import snitch.kofix.CreationLogic.hash
import snitch.kofix.CreationLogic.with
import java.util.Collections.*
import kotlin.reflect.*

/**
 * A delegate which creates a random list of the specified type. It must be used as a delegate
 * using the delegate property syntax:
 *
 * val randomUsers by aRandomListOf<User>()
 *
 * It works with generic types as well.
 */
class aRandomListOf<out T : Any>(
    private val size: Int? = null,
    private val minSize: Int = 1,
    private val maxSize: Int = 5,
    private val customization: List<T>.() -> List<T> = { this }
) {

    init {
        CreationLogic
    }

    private var t: List<T>? = null
    private var lastSeed = Seed.seed

    operator fun getValue(host: Any, property: KProperty<*>): List<T> {
        return if (t != null && lastSeed == Seed.seed) t!!
        else {
            val typeOfListItems = property.returnType.arguments.first().type!!
            val hostClassName = host::class.java.canonicalName
            val propertyName = property.name
            val list = aList(
                typeOfListItems,
                hostClassName.hash with propertyName.hash,
                emptySet(),
                kProperty = property,
                size = size?.dec(),
                minSize = minSize,
                maxSize = maxSize
            )
            (list as List<T>).let {
                lastSeed = Seed.seed
                val res = it
                t = customization(res)
                t as List<T>
            }
        }
    }
}

/**
 * A delegate which creates a random object of the specified type. It must be used as a delegate
 * using the delegate property syntax:
 *
 * val aUser by aRandom<User>()
 *
 * It works with generic types as well.
 */
class aRandom<out T : Any>(private val customization: T.() -> T = { this }) {

    val stackWalker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)

    init {
        CreationLogic
    }

    private var t: T? = null
    private var lastSeed = Seed.seed

    operator fun getValue(hostClass: Any, property: KProperty<*>): T {
        return if (t != null && lastSeed == Seed.seed) t!!
        else instantiateRandomClass(
            property.returnType,
            property,
            hostClass::class.java.canonicalName.hash with property.name.hash
        ).let {
            lastSeed = Seed.seed
            val res = it as T
            t = customization(res)
            return t as T
        }
    }

    operator fun getValue(hostClass: Nothing?, property: KProperty<*>): T {
        return if (t != null && lastSeed == Seed.seed) t!!
        else {
            instantiateRandomClass(
                property.returnType,
                property,
                stackWalker.walk{it.skip(1).findFirst().get().declaringClass}.canonicalName.hash with property.name.hash
            ).let {
                lastSeed = Seed.seed
                val res = it as T
                t = customization(res)
                return t as T
            }
        }
    }
}

fun instantiateRandomClass(type: KType, kProperty: KProperty<*>?, token: Long = 0): Any? =
    CreationLogic.instantiateRandomClass(type, token, kProperty = kProperty)

object _VirtualKProperty : KProperty<Any> {
    override val name: String
        get() = "virtual"
    override val annotations: List<Annotation>
        get() = TODO("Not yet implemented")
    override val getter: KProperty.Getter<Any>
        get() = TODO("Not yet implemented")
    override val isAbstract: Boolean
        get() = TODO("Not yet implemented")
    override val isConst: Boolean
        get() = TODO("Not yet implemented")
    override val isFinal: Boolean
        get() = TODO("Not yet implemented")
    override val isLateinit: Boolean
        get() = TODO("Not yet implemented")
    override val isOpen: Boolean
        get() = TODO("Not yet implemented")
    override val isSuspend: Boolean
        get() = TODO("Not yet implemented")
    override val parameters: List<KParameter>
        get() = TODO("Not yet implemented")
    override val returnType: KType
        get() = TODO("Not yet implemented")
    override val typeParameters: List<KTypeParameter>
        get() = TODO("Not yet implemented")
    override val visibility: KVisibility?
        get() = TODO("Not yet implemented")

    override fun call(vararg args: Any?): Any {
        TODO("Not yet implemented")
    }

    override fun callBy(args: Map<KParameter, Any?>): Any {
        TODO("Not yet implemented")
    }
}

