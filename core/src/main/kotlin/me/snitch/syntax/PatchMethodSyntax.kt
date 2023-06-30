package me.snitch.syntax

import me.snitch.parameters.ParametrizedPath
import me.snitch.parameters.PathParam
import me.snitch.types.HTTPMethods.PATCH

interface PatchMethodSyntax: MethodSyntax {
    fun PATCH() = method(PATCH)
    infix fun PATCH(path: String) = method(PATCH, path)
    infix fun PATCH(path: ParametrizedPath) = method(PATCH, path)
    infix fun PATCH(path: PathParam<out Any, out Any>) = PATCH("" / path)
}