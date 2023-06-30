package snitch.syntax

import snitch.parameters.ParametrizedPath
import snitch.parameters.PathParam
import snitch.types.HTTPMethods.POST

interface PostMethodSyntax : MethodSyntax {
    fun POST() = method(POST)
    infix fun POST(path: String) = method(POST, path)
    infix fun POST(path: ParametrizedPath) = method(POST, path)
    infix fun POST(path: PathParam<out Any, out Any>) = POST("" / path)
}