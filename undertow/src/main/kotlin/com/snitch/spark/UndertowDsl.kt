package com.snitch.spark

import io.undertow.server.HttpServerExchange
import io.undertow.util.PathTemplateMatch
import me.snitchon.*
import me.snitchon.parameters.HeaderParameter
import me.snitchon.parameters.PathParam
import me.snitchon.parameters.QueryParameter
import me.snitchon.parsing.Parser
import me.snitchon.request.RequestWrapper
import me.snitchon.request.filterValid
import me.snitchon.types.Format
import me.snitchon.types.HTTPMethods
import java.net.URLDecoder

class UndertowRequestWrapper(
    override val parser: Parser,
    val exchange: HttpServerExchange,
    inline val _body: () -> Any?,
) : RequestWrapper {

    override val body: () -> Any? get() = _body
    override fun method(): HTTPMethods = HTTPMethods.fromString(exchange.requestMethod.toString())

    override fun params(name: String): String? =
        URLDecoder.decode(exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY).parameters.get(name))

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