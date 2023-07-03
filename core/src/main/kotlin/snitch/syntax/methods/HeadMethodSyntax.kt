package snitch.syntax.methods

import snitch.parameters.ParametrizedPath
import snitch.parameters.PathParam
import snitch.syntax.MethodSyntax
import snitch.syntax.method
import snitch.types.HTTPMethods.HEAD

interface HeadMethodSyntax: MethodSyntax {
    fun HEAD() = method(HEAD)
    infix fun HEAD(path: String) = method(HEAD, path)
    infix fun HEAD(path: ParametrizedPath) = method(HEAD, path)
    infix fun HEAD(path: PathParam<out Any, out Any>) = HEAD("" / path)
}