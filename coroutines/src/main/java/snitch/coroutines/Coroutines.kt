package snitch.coroutines

import kotlinx.coroutines.runBlocking
import snitch.request.BodiedHandler
import snitch.request.Handler
import snitch.request.TypedRequestWrapper
import snitch.response.HttpResponse
import snitch.router.Router
import snitch.service.Endpoint
import snitch.types.HandlerResponse
import snitch.types.StatusCodes
import kotlin.reflect.full.starProjectedType

context (Router)
inline infix fun <B : Any, reified T : Any, reified S : StatusCodes> Endpoint<B>.isCoHandledBy(
    noinline handler: suspend TypedRequestWrapper<B>.() -> HttpResponse<T, S>
): Endpoint<B> {
    val regularHandler: TypedRequestWrapper<B>.() -> HttpResponse<T, S> = {
        val that = this
        runBlocking { handler(that) }
    }
    return addEndpoint(
        HandlerResponse(S::class.starProjectedType, T::class.starProjectedType, regularHandler)
    )
}

//context (Router)
//inline infix fun <B : Any, reified T : Any, S : StatusCodes> Endpoint<B>.isCoHandledBy(
//    handlerResponse: HandlerResponse<B, T, S>
//): Endpoint<B> = addEndpoint(handlerResponse)

fun <T, S : StatusCodes> coHandling(block: suspend TypedRequestWrapper<Nothing>.() -> HttpResponse<T, S>): Handler<Nothing, T, S> {
    val regularHandler: TypedRequestWrapper<Nothing>.() -> HttpResponse<T, S> = {
        val that = this
        runBlocking { block(that) }
    }
    return Handler<Nothing, T, S>(regularHandler)
}

infix fun <B : Any, T, S : StatusCodes> BodiedHandler<B>.coHandling(block: suspend TypedRequestWrapper<B>.() -> HttpResponse<T, S>): Handler<B, T, S> {
    val regularHandler: TypedRequestWrapper<B>.() -> HttpResponse<T, S> = {
        val that = this
        runBlocking { block(that) }
    }
    return Handler<B, T, S>(regularHandler)
}
