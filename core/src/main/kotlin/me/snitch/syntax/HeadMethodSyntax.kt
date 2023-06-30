package me.snitch.syntax

import me.snitch.parameters.ParametrizedPath
import me.snitch.parameters.PathParam
import me.snitch.types.HTTPMethods.HEAD

interface HeadMethodSyntax: MethodSyntax {
    fun HEAD() = method(HEAD)
    infix fun HEAD(path: String) = method(HEAD, path)
    infix fun HEAD(path: ParametrizedPath) = method(HEAD, path)
    infix fun HEAD(path: PathParam<out Any, out Any>) = HEAD("" / path)
}