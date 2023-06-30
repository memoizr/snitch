package snitch.syntax

import snitch.parameters.ParametrizedPath
import snitch.parameters.PathParam
import snitch.types.HTTPMethods.OPTIONS

interface OptionsMethodSyntax : MethodSyntax {
    fun OPTIONS() = method(OPTIONS)
    infix fun OPTIONS(path: String) = method(OPTIONS, path)
    infix fun OPTIONS(path: ParametrizedPath) = method(OPTIONS, path)
    infix fun OPTIONS(path: PathParam<out Any, out Any>) = OPTIONS("" / path)
}