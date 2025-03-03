package snitch.kofix

import snitch.kofix.PropertyBased.countOfInvocations
import kotlin.reflect.*

object PropertyBased{
    val countOfInvocations: MutableMap<KType, Long> = mutableMapOf()

}

inline fun <reified T> a(): T {
    val type = typeOf<T>()
    countOfInvocations[type] = countOfInvocations[type]?.inc() ?: 1
    return instantiateRandomClass(type, _VirtualKProperty, type.hashCode().toLong() + (countOfInvocations[type] ?: 0)).let {
        return it as T
    }
}

inline fun <reified T> any(): T = a()
inline fun <reified T> an(): T = a()
