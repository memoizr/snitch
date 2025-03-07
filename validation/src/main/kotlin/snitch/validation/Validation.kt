package snitch.validation

import jakarta.validation.Validation.buildDefaultValidatorFactory
import jakarta.validation.Validator
import jakarta.validation.ValidatorFactory
import snitch.types.DataClassValidator
import snitch.types.DataClassValidatorError
import snitch.types.ValidatedDataClass

class HibernateDataClassValidator(
    validationFactory: ValidatorFactory = buildDefaultValidatorFactory()
) : DataClassValidator {
    private val validator: Validator = validationFactory.validator

    override fun <T> validate(t: T): ValidatedDataClass {
        val violations = validator.validate(t)

        return if (violations.isEmpty()) {
            ValidatedDataClass.Valid(t)
        } else {
            val errors = violations.map { violation ->
                DataClassValidatorError("${violation.propertyPath}: ${violation.message}")
            }
            ValidatedDataClass.Invalid(errors)
        }
    }
}

