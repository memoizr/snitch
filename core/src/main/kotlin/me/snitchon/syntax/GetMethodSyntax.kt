package me.snitchon.syntax

import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.types.HTTPMethods.GET

interface GetMethodSyntax : MethodSyntax {
    fun GET() = method(GET)
    infix fun GET(path: String) = method(GET, path)
    infix fun GET(path: ParametrizedPath) = method(GET, path)
    infix fun GET(path: PathParam<out Any, out Any>) = GET("" / path)
}