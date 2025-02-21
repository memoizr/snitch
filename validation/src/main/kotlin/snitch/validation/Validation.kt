package snitch.validation

import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties

// ----- Define Annotations -----

@Target(AnnotationTarget.PROPERTY)
annotation class StringLength(val min: Int = 0, val max: Int = Int.MAX_VALUE)

@Target(AnnotationTarget.PROPERTY)
annotation class RegexPattern(val pattern: String)

@Target(AnnotationTarget.PROPERTY)
annotation class Length(val min: Int = 0, val max: Int = Int.MAX_VALUE)

@Target(AnnotationTarget.PROPERTY)
annotation class Min(val value: Double)

@Target(AnnotationTarget.PROPERTY)
annotation class Max(val value: Double)


// ----- The Validator Object -----

object Validator {

    /**
     * Validate all annotated fields in an object recursively.
     *
     * @param obj the object to validate
     * @return ValidationResult containing either the valid object or a list of validation errors
     */
    fun <T : Any> validate(obj: T): ValidationResult<T> {
        val errors = mutableListOf<ValidationError>()
        validateRecursively(obj, errors, "")
        return if (errors.isEmpty()) {
            ValidationResult.Valid(obj)
        } else {
            ValidationResult.Invalid(errors)
        }
    }

    private fun validateRecursively(obj: Any, errors: MutableList<ValidationError>, path: String) {
        // Iterate over each property of the object
        for (property in obj::class.memberProperties) {
            val propertyPath = if (path.isEmpty()) property.name else "$path.${property.name}"
            val value = property.getter.call(obj) ?: continue

            when (value) {
                is String -> {
                    // Validate String length if annotation is present
                    property.findAnnotation<StringLength>()?.let { stringLength ->
                        if (value.length < stringLength.min || value.length > stringLength.max) {
                            errors.add(ValidationError(
                                field = propertyPath,
                                message = "String length ${value.length} not in range [${stringLength.min}, ${stringLength.max}]"
                            ))
                        }
                    }
                    // Validate String pattern if annotation is present
                    property.findAnnotation<RegexPattern>()?.let { regex ->
                        if (!Regex(regex.pattern).matches(value)) {
                            errors.add(ValidationError(
                                field = propertyPath,
                                message = "Value '$value' does not match regex '${regex.pattern}'"
                            ))
                        }
                    }
                }
                is Collection<*> -> {
                    // Validate collection length if annotation is present
                    property.findAnnotation<Length>()?.let { length ->
                        if (value.size < length.min || value.size > length.max) {
                            errors.add(ValidationError(
                                field = propertyPath,
                                message = "Collection size ${value.size} not in range [${length.min}, ${length.max}]"
                            ))
                        }
                    }
                    // Recursively validate each element if it is not a primitive type
                    value.forEachIndexed { index, element ->
                        if (element != null && !isPrimitive(element)) {
                            validateRecursively(element, errors, "$propertyPath[$index]")
                        }
                    }
                }
                is Array<*> -> {
                    // Validate array length if annotation is present
                    property.findAnnotation<Length>()?.let { length ->
                        if (value.size < length.min || value.size > length.max) {
                            errors.add(ValidationError(
                                field = propertyPath,
                                message = "Array size ${value.size} not in range [${length.min}, ${length.max}]"
                            ))
                        }
                    }
                    // Recursively validate each element if it is not a primitive type
                    value.forEachIndexed { index, element ->
                        if (element != null && !isPrimitive(element)) {
                            validateRecursively(element, errors, "$propertyPath[$index]")
                        }
                    }
                }
                is Number -> {
                    // Validate minimum for numbers
                    property.findAnnotation<Min>()?.let { min ->
                        if (value.toDouble() < min.value) {
                            errors.add(ValidationError(
                                field = propertyPath,
                                message = "Value ${value} is less than minimum ${min.value}"
                            ))
                        }
                    }
                    // Validate maximum for numbers
                    property.findAnnotation<Max>()?.let { max ->
                        if (value.toDouble() > max.value) {
                            errors.add(ValidationError(
                                field = propertyPath,
                                message = "Value ${value} is greater than maximum ${max.value}"
                            ))
                        }
                    }
                }
                else -> {
                    // If the property is a complex object (and not a known primitive), validate recursively.
                    if (!isPrimitive(value)) {
                        validateRecursively(value, errors, propertyPath)
                    }
                }
            }
        }
    }

    /**
     * Checks if a value is considered a primitive for validation purposes.
     */
    private fun isPrimitive(value: Any): Boolean {
        return when (value) {
            is String, is Number, is Boolean, is Char -> true
            else -> false
        }
    }
}
