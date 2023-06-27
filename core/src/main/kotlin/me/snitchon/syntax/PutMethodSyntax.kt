package me.snitchon.syntax

import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.types.HTTPMethods.PUT

interface PutMethodSyntax: MethodSyntax {
    fun PUT() = method(PUT)
    infix fun PUT(path: String) = method(PUT, path)
    infix fun PUT(path: ParametrizedPath) = method(PUT, path)
    infix fun PUT(path: PathParam<out Any, out Any>) = PUT("" / path)
}