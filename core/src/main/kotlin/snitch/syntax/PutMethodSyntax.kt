package snitch.syntax

import snitch.types.HTTPMethods.PUT
import snitch.parameters.ParametrizedPath
import snitch.parameters.PathParam

interface PutMethodSyntax: MethodSyntax {
    fun PUT() = method(PUT)
    infix fun PUT(path: String) = method(PUT, path)
    infix fun PUT(path: ParametrizedPath) = method(PUT, path)
    infix fun PUT(path: PathParam<out Any, out Any>) = PUT("" / path)
}