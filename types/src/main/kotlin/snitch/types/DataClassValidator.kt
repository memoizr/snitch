package snitch.types

interface DataClassValidator {
    fun <T> validate(t: T): ValidatedDataClass
}

abstract class ValidatedDataClass {
    data class Valid<T>(val data: T): ValidatedDataClass()
    data class Invalid(val errors: List<DataClassValidatorError>): ValidatedDataClass()
}

data class DataClassValidatorError(val error: String)
