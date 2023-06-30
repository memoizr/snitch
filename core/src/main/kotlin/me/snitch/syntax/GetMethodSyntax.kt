package me.snitch.syntax

import me.snitch.parameters.ParametrizedPath
import me.snitch.parameters.PathParam
import me.snitch.types.HTTPMethods.GET

interface GetMethodSyntax : MethodSyntax {
    fun GET() = method(GET)
    infix fun GET(path: String) = method(GET, path)
    infix fun GET(path: ParametrizedPath) = method(GET, path)
    infix fun GET(path: PathParam<out Any, out Any>) = GET("" / path)
}