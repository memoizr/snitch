package snitch.syntax.methods

import snitch.parameters.ParametrizedPath
import snitch.parameters.PathParam
import snitch.syntax.MethodSyntax
import snitch.syntax.method
import snitch.types.HTTPMethods.PUT

interface PutMethodSyntax: MethodSyntax {
    fun PUT() = method(PUT)
    infix fun PUT(path: String) = method(PUT, path)
    infix fun PUT(path: ParametrizedPath) = method(PUT, path)
    infix fun PUT(path: PathParam<out Any, out Any>) = PUT("" / path)
}