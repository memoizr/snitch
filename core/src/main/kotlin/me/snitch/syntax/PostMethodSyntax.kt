package me.snitch.syntax

import me.snitch.parameters.ParametrizedPath
import me.snitch.parameters.PathParam
import me.snitch.types.HTTPMethods.POST

interface PostMethodSyntax : MethodSyntax {
    fun POST() = method(POST)
    infix fun POST(path: String) = method(POST, path)
    infix fun POST(path: ParametrizedPath) = method(POST, path)
    infix fun POST(path: PathParam<out Any, out Any>) = POST("" / path)
}