package me.snitchon.syntax

import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.types.HTTPMethods.OPTIONS

interface OptionsMethodSyntax : MethodSyntax {
    fun OPTIONS() = method(OPTIONS)
    infix fun OPTIONS(path: String) = method(OPTIONS, path)
    infix fun OPTIONS(path: ParametrizedPath) = method(OPTIONS, path)
    infix fun OPTIONS(path: PathParam<out Any, out Any>) = OPTIONS("" / path)
}