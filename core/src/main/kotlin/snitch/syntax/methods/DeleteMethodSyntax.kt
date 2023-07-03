package snitch.syntax.methods

import snitch.parameters.ParametrizedPath
import snitch.parameters.PathParam
import snitch.syntax.MethodSyntax
import snitch.syntax.method
import snitch.types.HTTPMethods.DELETE

interface DeleteMethodSyntax : MethodSyntax {
    fun DELETE() = method(DELETE)
    infix fun DELETE(path: String) = method(DELETE, path)
    infix fun DELETE(path: ParametrizedPath) = method(DELETE, path)
    infix fun DELETE(path: PathParam<out Any, out Any>) = DELETE("" / path)
}