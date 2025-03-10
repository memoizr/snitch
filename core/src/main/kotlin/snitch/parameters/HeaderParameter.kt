package snitch.parameters

import snitch.documentation.Visibility
import snitch.validation.Validator
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
) : Parameter<T, R>(
    type = type,
    name = name,
    pattern = pattern,
    description = description,
    required = required,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
)

data class HeaderParam<T, R>(
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: Visibility
) : HeaderParameter<T, R>(
    type = type,
    name = name,
    pattern = pattern,
    description = description,
    required = true,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing
)

data class OptionalHeaderParam<T, R>(
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    override val default: R,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: Visibility
) : HeaderParameter<T, R>(
    type = type,
    name = name,
    pattern = pattern,
    description = description,
    required = false,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing
)
, OptionalParam<R>

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
    private var param: OptionalHeaderParam<T, R>? = null
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
