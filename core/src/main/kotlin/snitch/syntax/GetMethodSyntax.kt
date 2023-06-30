package snitch.syntax

import snitch.types.HTTPMethods.GET
import snitch.parameters.ParametrizedPath
import snitch.parameters.PathParam

interface GetMethodSyntax : MethodSyntax {
    fun GET() = method(GET)
    infix fun GET(path: String) = method(GET, path)
    infix fun GET(path: ParametrizedPath) = method(GET, path)
    infix fun GET(path: PathParam<out Any, out Any>) = GET("" / path)
}