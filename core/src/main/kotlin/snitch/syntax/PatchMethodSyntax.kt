package snitch.syntax

import snitch.types.HTTPMethods.PATCH
import snitch.parameters.ParametrizedPath
import snitch.parameters.PathParam

interface PatchMethodSyntax: MethodSyntax {
    fun PATCH() = method(PATCH)
    infix fun PATCH(path: String) = method(PATCH, path)
    infix fun PATCH(path: ParametrizedPath) = method(PATCH, path)
    infix fun PATCH(path: PathParam<out Any, out Any>) = PATCH("" / path)
}