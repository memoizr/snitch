package me.snitchon.request

import me.snitchon.parsing.Parser
import me.snitchon.response.HttpResponse
import me.snitchon.types.HandlerResponse
import me.snitchon.types.StatusCodes
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

class Handler<Request : Any, Response, S : StatusCodes>(val block: context(Parser) Context<Request>.() -> HttpResponse<Response, S>) {
    operator fun getValue(
        nothing: Nothing?,
        property: KProperty<*>
    ): HandlerResponse<Request, Response, S> {
        val type = property.returnType.arguments.get(1).type
        val statusCode = property.returnType.arguments.get(2).type

        return HandlerResponse(statusCode!!, type!!, block)
    }
}
class BodiedHandler<B: Any> {
    infix fun <T, S: StatusCodes> handle(block: context(Parser) Context<B>.() -> HttpResponse<T, S>) = Handler<B, T,S>(block)
    infix fun <T, S: StatusCodes> thenHandling(block: context(Parser) Context<B>.() -> HttpResponse<T, S>) = Handler<B, T,S>(block)
}
fun <B: Any, T, S: StatusCodes> handle(b: KClass<B>, block: context(Parser) Context<B>.() -> HttpResponse<T, S>) = Handler<B, T,S>(block)
fun <B: Any, T, S: StatusCodes> handle(b: Function<B>, block: context(Parser) Context<B>.() -> HttpResponse<T, S>) = Handler<B, T,S>(block)
fun <T, S: StatusCodes> handle(block: context(Parser) Context<Nothing>.() -> HttpResponse<T, S>) = Handler<Nothing, T,S>(block)

inline fun <reified B: Any> parsing() = BodiedHandler<B>()
fun noBody() = BodiedHandler<Nothing>()

