package snitch.kofix

import snitch.kofix.CreationLogic.ObjectFactory
import kotlin.reflect.KProperty
import kotlin.reflect.KType
import kotlin.reflect.typeOf

inline fun <reified T> customize(noinline constructorBlock: Property.() -> T): Customization<T> {
    val type = typeOf<T>()
    return Customization(type, constructorBlock)
}

class Customization<T>(
    type: KType,
    val constructorBlock: Property.() -> T
) {
    val countOfInvocations = mutableMapOf<KType, Long>()

    init {
        ObjectFactory[type] = { _, _, kproperty, token ->
            constructorBlock(Property(kproperty!!, countOfInvocations, token)) as Any
        }
    }
}

class Property(val property: KProperty<*>, val _invocations: MutableMap<KType, Long>, val _token: Long) {
    val countOfInvocations = mutableMapOf<KType, Long>()

    inline fun <reified T> a(): T {
        val type = typeOf<T>()
        if (property is _VirtualKProperty)
            _invocations[type] = _invocations[type]?.inc() ?: 1
        else countOfInvocations[type] = countOfInvocations[type]?.inc() ?: 1
        return instantiateRandomClass(
            type,
            property,
            (if (property is _VirtualKProperty)
            property.name.hashCode() + _invocations[type]!!.toLong()
            else property.name.hashCode() + countOfInvocations[type]!!.toLong()
                    ) + _token
        ).let {
            return it as T
        }
    }
}


