package me.snitchon.parameters

import me.snitchon.validation.Validator
import kotlin.reflect.KProperty

data class PathParam<T, R>(
    val path: String? = null,
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String
) : Parameter<T, R>(
    type = type,
    name = name,
    pattern = pattern,
    description = description,
    required = true,
    emptyAsMissing = true,
    invalidAsMissing = false
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PathParam<*, *>

        if (path != other.path) return false
        if (type != other.type) return false
        if (name != other.name) return false
        return pattern == other.pattern
    }

    override fun hashCode(): Int {
        var result = path?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + pattern.hashCode()
        return result
    }
}

class PathParamDelegate<T, R>(
    val type: Class<*>,
    val name: String,
    val pattern: Validator<T, R>,
    val description: String,
) {
    private var param: PathParam<T, R>? = null
    operator fun getValue(nothing: Nothing?, property: KProperty<*>): PathParam<T, R> = param(property)
    operator fun getValue(nothing: Any?, property: KProperty<*>): PathParam<T, R> = param(property)

    private fun param(property: KProperty<*>) =
        param ?: PathParam(
            "{${name.ifEmpty { property.name }}}",
            type,
            name.ifEmpty { property.name },
            pattern,
            description,
        ).also { param = it }
}

