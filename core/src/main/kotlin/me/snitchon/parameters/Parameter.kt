package me.snitchon.parameters

import me.snitchon.documentation.Visibility
import com.snitch.me.snitchon.Validator
import kotlin.reflect.KProperty

sealed class Parameter<T, R>(
    open val type: Class<*>,
    open val name: String,
    open val pattern: Validator<T, R>,
    open val description: String,
    open val required: Boolean = false,
    open val emptyAsMissing: Boolean = false,
    open val invalidAsMissing: Boolean = false
)

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

data class PathParam<T, R>(
    val path: String? = null,
    override val type: Class<*>,
    override val name: String,
    override val pattern: Validator<T, R>,
    override val description: String
) : Parameter<T, R>(type, name, pattern, description, true, false) {

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

    operator fun getValue(
        nothing: Nothing?,
        property: KProperty<*>
    ) = PathParam(
        "{${name.ifEmpty { property.name }}}",
        type,
        name.ifEmpty { property.name },
        pattern,
        description,
    )
    operator fun getValue(
        nothing: Any?,
        property: KProperty<*>
    ) = PathParam(
        "{${name.ifEmpty { property.name }}}",
        type,
        name.ifEmpty { property.name },
        pattern,
        description,
    )
}

data class HeaderParam<T, R>(
    override val type: Class<*>,
    override val name: String,
    override inline val pattern: Validator<T, R>,
    override val description: String,
    override val emptyAsMissing: Boolean,
    override val invalidAsMissing: Boolean,
    override val visibility: Visibility
) : HeaderParameter<T, R>(type, name, pattern, description, true, emptyAsMissing, invalidAsMissing)

class HeaderParamDelegate<T, R>(
    val type: Class<*>,
    val name: String,
    val pattern: Validator<T, R>,
    val description: String,
    val emptyAsMissing: Boolean,
    val invalidAsMissing: Boolean,
    val visibility: Visibility
) {
    operator fun getValue(
        nothing: Nothing?,
        property: KProperty<*>
    ) = HeaderParam(
        type,
        name.ifEmpty { property.name },
        pattern,
        description,
        emptyAsMissing,
        invalidAsMissing,
        visibility
    )
}

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

inline fun <reified T, R> optionalQuery(
    name: String,
    description: String = "",
    condition: Validator<T, R>,
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) =
    OptionalQueryParam(
        T::class.java,
        name,
        condition.optional(),
        description,
        default = null,
        emptyAsMissing = emptyAsMissing,
        invalidAsMissing = invalidAsMissing,
        visibility = visibility
    )

inline fun <reified T, R> optionalQuery(
    name: String,
    description: String = "",
    condition: Validator<T, R>,
    default: R,
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) =
    OptionalQueryParam(
        T::class.java,
        name,
        condition,
        description,
        default = default,
        emptyAsMissing = emptyAsMissing,
        invalidAsMissing = invalidAsMissing,
        visibility = visibility
    )

inline fun <reified T, R> query(
    name: String,
    description: String = "",
    condition: Validator<T, R>,
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) =
    QueryParam(
        T::class.java,
        name,
        condition,
        description,
        emptyAsMissing = emptyAsMissing,
        invalidAsMissing = invalidAsMissing,
        visibility = visibility
    )

inline fun <reified T, R> optionalHeader(
    name: String,
    description: String = "",
    condition: Validator<T, R>,
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) =
    OptionalHeaderParam(
        T::class.java,
        name,
        condition.optional(),
        description,
        default = null,
        emptyAsMissing = emptyAsMissing,
        invalidAsMissing = invalidAsMissing,
        visibility = visibility
    )

inline fun <reified T, R> optionalHeader(
    name: String,
    description: String = "",
    condition: Validator<T, R>,
    emptyAsMissing: Boolean = false,
    default: R,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) =
    OptionalHeaderParam(
        T::class.java,
        name,
        condition,
        description,
        default = default,
        emptyAsMissing = emptyAsMissing,
        invalidAsMissing = invalidAsMissing,
        visibility = visibility
    )

inline fun <reified T, R> header(
    name: String,
    condition: Validator<T, R>,
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = HeaderParam(
    T::class.java,
    name,
    condition,
    description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> headerParam(
    name: String = "",
    condition: Validator<T, R>,
    description: String = "",
    emptyAsMissing: Boolean = false,
    invalidAsMissing: Boolean = false,
    visibility: Visibility = Visibility.PUBLIC
) = HeaderParamDelegate(
    T::class.java,
    name,
    condition,
    description,
    emptyAsMissing = emptyAsMissing,
    invalidAsMissing = invalidAsMissing,
    visibility = visibility
)

inline fun <reified T, R> path(name: String, description: String = "", condition: Validator<T, R>) = PathParam(
    "{${name}}",
    T::class.java,
    name,
    condition,
    description
)
inline fun <reified T, R> pathParam(condition: Validator<T, R>, name: String = "", description: String = "", ) = PathParamDelegate(
    T::class.java,
    name,
    condition,
    description
)
