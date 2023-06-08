package com.snitch.spark

import io.undertow.server.HttpServerExchange
import io.undertow.util.PathTemplateMatch
import me.snitchon.*
import java.net.URLDecoder

class UndertowRequestWrapper(val exchange: HttpServerExchange, inline val _body: () -> Any?) : RequestWrapper {

    override val body: () -> Any? get() = _body

    override fun method(): HTTPMethod = HTTPMethod.fromString(exchange.requestMethod.toString())

    override fun params(name: String): String? =
        URLDecoder.decode(exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY).parameters.get(name))

//    override fun <RAW, PARSED : Any?> getParam(param: Parameter<RAW, PARSED>): String? {
//        return when (param) {
//            is Path<*> -> {
//                val params = exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY)
//                URLDecoder.decode(params.parameters.get(param.name))
//            }
//
//            is Query<*, *> -> exchange.queryParameters.get(param.name)?.firstOrNull()
//            is Header<*, *> ->
//            else -> TODO()
//        }
//    }

    override fun headers(name: String): String? = exchange.requestHeaders.get(name)?.firstOrNull()

    override fun queryParams(name: String): String? = exchange.queryParameters.get(name)?.firstOrNull()

    override fun getPathParam(param: PathParam<*, *>): String? =
        params(param.name).filterValid(param)

    override fun getQueryParam(param: QueryParameter<*, *>) =
        queryParams(param.name).filterValid(param)

    override fun getHeaderParam(param: HeaderParameter<*, *>) =
        headers(param.name).filterValid(param)
}


class UndertowResponseWrapper(val exchange: HttpServerExchange) : ResponseWrapper {
    override fun setStatus(code: Int) = Unit
    override fun setType(type: Format) = Unit
}