package me.snitchon.parameters

import me.snitchon.documentation.Visibility
import me.snitchon.validation.Validator
import kotlin.reflect.KProperty

sealed class QueryParameter<T, R>(
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    override val required: Boolean = false,
    override val emptyAsMissing: Boolean = false,
    override val invalidAsMissing: Boolean = false,
    open val visibility: Visibility = Visibility.PUBLIC
) : Parameter<T, R>(type, name, pattern, description, required, emptyAsMissing)

data class OptionalQueryParam<T, R>(
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    val default: R,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: Visibility
) : QueryParameter<T, R>(type, name, pattern, description, false, emptyAsMissing, invalidAsMissing)

data class QueryParam<T, R>(
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: Visibility
) : QueryParameter<T, R>(type, name, pattern, description, true, emptyAsMissing, invalidAsMissing)

class QueryParamDelegate<T, R>(
    val type: Class<*>,
    val name: String,
    val pattern: Validator<T, R>,
    val description: String,
    val emptyAsMissing: Boolean,
    val invalidAsMissing: Boolean,
    val visibility: Visibility
) {
    var param: QueryParam<T, R>? = null
    operator fun getValue(nothing: Nothing?, property: KProperty<*>) = param(property)
    operator fun getValue(nothing: Any?, property: KProperty<*>) = param(property)

    private fun param(property: KProperty<*>) =
        param ?: QueryParam(
            type,
            name.ifEmpty { property.name },
            pattern,
            description,
            emptyAsMissing,
            invalidAsMissing,
            visibility
        ).also { param = it }
}

class OptionalQueryParamDelegate<T, R>(
    val type: Class<*>,
    val name: String,
    val pattern: Validator<T, R>,
    val description: String,
    val default: R,
    val emptyAsMissing: Boolean,
    val invalidAsMissing: Boolean,
    val visibility: Visibility
) {
    private var param: OptionalQueryParam<T, R>? = null
    operator fun getValue(nothing: Nothing?, property: KProperty<*>) = param(property)
    operator fun getValue(nothing: Any?, property: KProperty<*>) = param(property)

    private fun param(property: KProperty<*>) =
        param ?: OptionalQueryParam(
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
