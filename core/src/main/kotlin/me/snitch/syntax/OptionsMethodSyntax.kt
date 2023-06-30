package me.snitch.syntax

import me.snitch.parameters.ParametrizedPath
import me.snitch.parameters.PathParam
import me.snitch.types.HTTPMethods.OPTIONS

interface OptionsMethodSyntax : MethodSyntax {
    fun OPTIONS() = method(OPTIONS)
    infix fun OPTIONS(path: String) = method(OPTIONS, path)
    infix fun OPTIONS(path: ParametrizedPath) = method(OPTIONS, path)
    infix fun OPTIONS(path: PathParam<out Any, out Any>) = OPTIONS("" / path)
}