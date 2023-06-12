package me.snitchon.request

import me.snitchon.extensions.print
import me.snitchon.parsing.Parser
import me.snitchon.response.HttpResponse
import me.snitchon.types.HandlerResponse
import me.snitchon.types.StatusCodes
import kotlin.reflect.KProperty

class Handler<Request : Any, Response, S : StatusCodes>(val block: context(Parser) Context<Request>.() -> HttpResponse<Response, S>) {
    operator fun getValue(
        nothing: Nothing?,
        property: KProperty<*>
    ): HandlerResponse<Request, Response, S> {
        val type = property.returnType.arguments.get(1).type.print()
        val statusCode = property.returnType.arguments.get(2).type

        return HandlerResponse(statusCode!!, type!!, block)
    }
}