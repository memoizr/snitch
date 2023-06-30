package snitch.example.types

class ValidationException(val reason: String) : Exception(reason)
class ForbiddenException() : Exception()
