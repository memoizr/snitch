package snitch.validation

class ValidationException(val value: Any, val exception: Exception? = null): Exception()