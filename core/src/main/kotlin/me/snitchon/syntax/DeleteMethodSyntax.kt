package me.snitchon.syntax

import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.types.HTTPMethods.DELETE

interface DeleteMethodSyntax : MethodSyntax {
    fun DELETE() = method(DELETE)
    infix fun DELETE(path: String) = method(DELETE, path)
    infix fun DELETE(path: ParametrizedPath) = method(DELETE, path)
    infix fun DELETE(path: PathParam<out Any, out Any>) = DELETE("" / path)
}