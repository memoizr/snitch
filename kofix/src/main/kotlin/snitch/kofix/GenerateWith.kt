package snitch.kofix

import kotlin.reflect.KProperty

inline fun <reified T, reified A, reified B> custom(noinline custom: (A, B) -> T) = Generator1(custom)
class Generator1<T, A, B>(private val custom: (A, B) -> T) {

    operator fun getValue(a: Any, property: KProperty<*>): Generator1<T, A, B> {
        val type = property.returnType.arguments.first().type!!
        val type1 = property.returnType.arguments[1].type!!
        val type2 = property.returnType.arguments[2].type!!
        CreationLogic.ObjectFactory[type] = { _, _, property, token -> custom(
                CreationLogic.instantiateRandomClass(type1, token = token, kProperty = property) as A,
                CreationLogic.instantiateRandomClass(type2, token = token, kProperty = property) as B) as Any }
        return this
    }
}

inline fun <reified T, A> custom(noinline custom: (A) -> T) = Generator0(custom)
inline fun <reified T> custom(noinline custom: () -> T) = Generator0<T, Any> {custom()}

class Generator0<T, A>(private val custom: (A) -> T) {
    operator fun getValue(a: Any, property: KProperty<*>): Generator0<T, A> {
        val type = property.returnType.arguments.first().type!!
        val type1 = property.returnType.arguments[1].type!!
        CreationLogic.ObjectFactory[type] = { _, _, property, token -> custom(
            CreationLogic.instantiateRandomClass(
                type1,
                token = token,
                kProperty = property
            ) as A) as Any }
        return this
    }
}
