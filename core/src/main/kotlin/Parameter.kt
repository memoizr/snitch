package com.snitch

import com.snitch.documentation.Visibility

sealed class Parameter<T>(
        open val type: Class<*>,
        open val name: String,
        open val pattern: Validator<T>,
        open val description: String,
        open val required: Boolean = false,
        open val emptyAsMissing: Boolean = false,
        open val invalidAsMissing: Boolean = false
)

sealed class HeaderParameter<T>(
        override val type: Class<*>,
        override val name: String,
        override val pattern: Validator<T>,
        override val description: String,
        override val required: Boolean = false,
        override val emptyAsMissing: Boolean = false,
        override val invalidAsMissing: Boolean = false,
        open val visibility: Visibility = Visibility.PUBLIC
) : Parameter<T>(type, name, pattern, description, required, emptyAsMissing)

sealed class QueryParameter<T>(
        override val type: Class<*>,
        override val name: String,
        override val pattern: Validator<T>,
        override val description: String,
        override val required: Boolean = false,
        override val emptyAsMissing: Boolean = false,
        override val invalidAsMissing: Boolean = false,
        open val visibility: Visibility = Visibility.PUBLIC
) : Parameter<T>(type, name, pattern, description, required, emptyAsMissing)

data class PathParam<T>(
        val path: String? = null,
        override val type: Class<*>,
        override val name: String,
        override val pattern: Validator<T>,
        override val description: String) : Parameter<T>(type, name, pattern, description, true, false) {
    operator fun div(path: String) = this.path + "/" + path
}

data class HeaderParam<T>(
        override val type: Class<*>,
        override val name: String,
        override val pattern: Validator<T>,
        override val description: String,
        override val emptyAsMissing: Boolean,
        override val invalidAsMissing: Boolean,
        override val visibility: Visibility) : HeaderParameter<T>(type, name, pattern, description, true, emptyAsMissing, invalidAsMissing)

data class OptionalHeaderParam<T>(
        override val type: Class<*>,
        override val name: String,
        override val pattern: Validator<T>,
        override val description: String,
        val default: T,
        override val emptyAsMissing: Boolean,
        override val invalidAsMissing: Boolean,
        override val visibility: Visibility) : HeaderParameter<T>(type, name, pattern, description, false, emptyAsMissing, invalidAsMissing)

data class OptionalQueryParam<T>(
        override val type: Class<*>,
        override val name: String,
        override val pattern: Validator<T>,
        override val description: String,
        val default: T,
        override val emptyAsMissing: Boolean,
        override val invalidAsMissing: Boolean,
        override val visibility: Visibility) : QueryParameter<T>(type, name, pattern, description, false, emptyAsMissing, invalidAsMissing)

data class QueryParam<T>(
        override val type: Class<*>,
        override val name: String,
        override val pattern: Validator<T>,
        override val description: String,
        override val emptyAsMissing: Boolean,
        override val invalidAsMissing: Boolean,
        override val visibility: Visibility
) : QueryParameter<T>(type, name, pattern, description, true, emptyAsMissing, invalidAsMissing)

inline fun <reified T> optionalQuery(name: String,
                                     description: String,
                                     condition: Validator<T>,
                                     emptyAsMissing: Boolean = false,
                                     invalidAsMissing: Boolean = false,
                                     visibility: Visibility = Visibility.PUBLIC) =
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

inline fun <reified T> optionalQuery(name: String,
                                     description: String,
                                     condition: Validator<T>,
                                     default: T,
                                     emptyAsMissing: Boolean = false,
                                     invalidAsMissing: Boolean = false,
                                     visibility: Visibility = Visibility.PUBLIC) =
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

inline fun <reified T> query(name: String,
                             description: String,
                             condition: Validator<T>,
                             emptyAsMissing: Boolean = false,
                             invalidAsMissing: Boolean = false,
                             visibility: Visibility = Visibility.PUBLIC) =
        QueryParam(
                T::class.java,
                name,
                condition,
                description,
                emptyAsMissing = emptyAsMissing,
                invalidAsMissing = invalidAsMissing,
                visibility = visibility
        )

inline fun <reified T> optionalHeader(
        name: String,
        description: String,
        condition: Validator<T>,
        emptyAsMissing: Boolean = false,
        invalidAsMissing: Boolean = false,
        visibility: Visibility = Visibility.PUBLIC) =
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

inline fun <reified T> optionalHeader(
        name: String,
        description: String,
        condition: Validator<T>,
        emptyAsMissing: Boolean = false,
        default: T,
        invalidAsMissing: Boolean = false,
        visibility: Visibility = Visibility.PUBLIC) =
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

inline fun <reified T> header(
        name: String,
        description: String,
        condition: Validator<T>,
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

inline fun <reified T> path(name: String, description: String, condition: Validator<T>) = PathParam(
        null,
        T::class.java,
        name,
        condition,
        description)
