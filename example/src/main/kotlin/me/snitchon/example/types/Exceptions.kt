package me.snitchon.example.types

class ValidationException(val reason: String) : Exception(reason)
class ForbiddenException() : Exception()
