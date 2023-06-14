package me.snitchon.parameters

class InvalidParametersException(
    val e: Throwable,
    val reasons: List<String>): Exception(e)

