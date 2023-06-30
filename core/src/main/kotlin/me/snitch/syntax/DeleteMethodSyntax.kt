package me.snitch.syntax

import me.snitch.parameters.ParametrizedPath
import me.snitch.parameters.PathParam
import me.snitch.types.HTTPMethods.DELETE

interface DeleteMethodSyntax : MethodSyntax {
    fun DELETE() = method(DELETE)
    infix fun DELETE(path: String) = method(DELETE, path)
    infix fun DELETE(path: ParametrizedPath) = method(DELETE, path)
    infix fun DELETE(path: PathParam<out Any, out Any>) = DELETE("" / path)
}