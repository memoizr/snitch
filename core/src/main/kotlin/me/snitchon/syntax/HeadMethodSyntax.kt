package me.snitchon.syntax

import me.snitchon.router.leadingSlash
import me.snitchon.parameters.ParametrizedPath
import me.snitchon.parameters.PathParam
import me.snitchon.request.Body
import me.snitchon.service.Endpoint
import me.snitchon.types.HTTPMethods.HEAD

interface HeadMethodSyntax: MethodSyntax {
    fun HEAD() = method(HEAD)
    infix fun HEAD(path: String) = method(HEAD, path)
    infix fun HEAD(path: ParametrizedPath) = method(HEAD, path)
    infix fun HEAD(path: PathParam<out Any, out Any>) = HEAD("" / path)
}