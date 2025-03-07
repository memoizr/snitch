package snitch.validation

import snitch.router.Router
import snitch.router.decorateWith
import snitch.types.ErrorResponse
import snitch.types.StatusCodes
import snitch.types.ValidatedDataClass


val Router.validated
    get() = decorateWith {
        val validationResult = ValidatorModule.validator().validate(body())
        when (validationResult) {
            is ValidatedDataClass.Valid<*> -> next()
            is ValidatedDataClass.Invalid -> ErrorResponse(400, validationResult.errors).badRequest<Any, Any, StatusCodes>()
        }
    }
