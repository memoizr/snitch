package me.snitchon.syntax

import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.types.HTTPMethods.PATCH

interface PatchMethodSyntax: MethodSyntax {
    fun PATCH() = method(PATCH)
    infix fun PATCH(path: String) = method(PATCH, path)
    infix fun PATCH(path: ParametrizedPath) = method(PATCH, path)
    infix fun PATCH(path: PathParam<out Any, out Any>) = PATCH("" / path)
}