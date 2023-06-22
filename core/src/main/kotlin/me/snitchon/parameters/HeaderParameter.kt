package me.snitchon.parameters

import com.snitch.me.snitchon.Validator
import me.snitchon.documentation.Visibility
import kotlin.reflect.KProperty

sealed class HeaderParameter<T, R>(
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    override val required: Boolean = false,
    override val emptyAsMissing: Boolean = false,
    override val invalidAsMissing: Boolean = false,
    open val visibility: Visibility = Visibility.PUBLIC
) : Parameter<T, R>(type, name, pattern, description, required, emptyAsMissing)

data class HeaderParam<T, R>(
    override val type: Class<*>,
    override val name: String,
    override inline val pattern: Validator<T, R>,
    override val description: String,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: Visibility
) : HeaderParameter<T, R>(type, name, pattern, description, true, emptyAsMissing, invalidAsMissing)

data class OptionalHeaderParam<T, R>(
    override val type: Class<*>,
    override inline val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    val default: R,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: Visibility
) : HeaderParameter<T, R>(type, name, pattern, description, false, emptyAsMissing, invalidAsMissing)

class HeaderParamDelegate<T, R>(
    val type: Class<*>,
    val name: String,
    val pattern: Validator<T, R>,
    val description: String,
    val emptyAsMissing: Boolean,
    val invalidAsMissing: Boolean,
    val visibility: Visibility
) {
    private var param: HeaderParam<T, R>? = null
    operator fun getValue(nothing: Nothing?, property: KProperty<*>) = param(property)
    operator fun getValue(nothing: Any?, property: KProperty<*>) = param(property)

    private fun param(property: KProperty<*>) =
        param ?: HeaderParam(
            type,
            name.ifEmpty { property.name },
            pattern,
            description,
            emptyAsMissing,
            invalidAsMissing,
            visibility
        ).also { param = it }
}

class OptionalHeaderParamDelegate<T, R>(
    val type: Class<*>,
    val name: String,
    val pattern: Validator<T, R>,
    val description: String,
    val default: R,
    val emptyAsMissing: Boolean,
    val invalidAsMissing: Boolean,
    val visibility: Visibility
) {
    var param: OptionalHeaderParam<T, R>? = null
    operator fun getValue(nothing: Nothing?, property: KProperty<*>) = param(property)
    operator fun getValue(nothing: Any?, property: KProperty<*>) = param(property)

    private fun param(property: KProperty<*>) =
        param ?: OptionalHeaderParam(
            type,
            name.ifEmpty { property.name },
            pattern,
            description,
            default,
            emptyAsMissing,
            invalidAsMissing,
            visibility
        ).also { param = it }
}
