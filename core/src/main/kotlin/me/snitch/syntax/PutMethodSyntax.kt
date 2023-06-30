package me.snitch.syntax

import me.snitch.parameters.ParametrizedPath
import me.snitch.parameters.PathParam
import me.snitch.types.HTTPMethods.PUT

interface PutMethodSyntax: MethodSyntax {
    fun PUT() = method(PUT)
    infix fun PUT(path: String) = method(PUT, path)
    infix fun PUT(path: ParametrizedPath) = method(PUT, path)
    infix fun PUT(path: PathParam<out Any, out Any>) = PUT("" / path)
}