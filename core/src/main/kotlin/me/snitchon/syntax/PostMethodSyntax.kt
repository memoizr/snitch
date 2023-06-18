package me.snitchon.syntax

import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.types.HTTPMethods.POST

interface PostMethodSyntax : MethodSyntax {
    fun POST() = method(POST)
    infix fun POST(path: String) = method(POST, path)
    infix fun POST(path: ParametrizedPath) = method(POST, path)
    infix fun POST(path: PathParam<out Any, out Any>) = POST("" / path)
}