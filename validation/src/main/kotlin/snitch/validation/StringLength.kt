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
     * @return a list of error messages if validations fail, empty list otherwise.
     */
    fun validate(obj: Any): List<String> {
        val errors = mutableListOf<String>()
        validateRecursively(obj, errors, "")
        return errors
    }

    private fun validateRecursively(obj: Any, errors: MutableList<String>, path: String) {
        // Iterate over each property of the object
        for (property in obj::class.memberProperties) {
            val propertyPath = if (path.isEmpty()) property.name else "$path.${property.name}"
            val value = property.getter.call(obj) ?: continue

            when (value) {
                is String -> {
                    // Validate String length if annotation is present
                    property.findAnnotation<StringLength>()?.let { stringLength ->
                        if (value.length < stringLength.min || value.length > stringLength.max) {
                            errors.add("$propertyPath: String length ${value.length} not in range [${stringLength.min}, ${stringLength.max}].")
                        }
                    }
                    // Validate String pattern if annotation is present
                    property.findAnnotation<RegexPattern>()?.let { regex ->
                        if (!Regex(regex.pattern).matches(value)) {
                            errors.add("$propertyPath: '$value' does not match regex '${regex.pattern}'.")
                        }
                    }
                }
                is Collection<*> -> {
                    // Validate collection length if annotation is present
                    property.findAnnotation<Length>()?.let { length ->
                        if (value.size < length.min || value.size > length.max) {
                            errors.add("$propertyPath: Collection size ${value.size} not in range [${length.min}, ${length.max}].")
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
                            errors.add("$propertyPath: Array size ${value.size} not in range [${length.min}, ${length.max}].")
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
                            errors.add("$propertyPath: Value ${value} is less than minimum ${min.value}.")
                        }
                    }
                    // Validate maximum for numbers
                    property.findAnnotation<Max>()?.let { max ->
                        if (value.toDouble() > max.value) {
                            errors.add("$propertyPath: Value ${value} is greater than maximum ${max.value}.")
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
