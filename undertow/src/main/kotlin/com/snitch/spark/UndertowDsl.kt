package com.snitch.spark

import io.undertow.server.HttpServerExchange
import io.undertow.util.PathTemplateMatch
import me.snitchon.parameters.HeaderParameter
import me.snitchon.parameters.Parameter
import me.snitchon.parameters.PathParam
import me.snitchon.parameters.QueryParameter
import me.snitchon.parsing.Parser
import me.snitchon.request.ImplementationRequestWrapper
import me.snitchon.request.filterValid
import me.snitchon.types.HTTPMethods
import java.net.URLDecoder

class UndertowRequestWrapper(
    override val parser: Parser,
    override val params: Set<Parameter<*, *>>,
    val exchange: HttpServerExchange,
    inline val _body: () -> Any?,
) : ImplementationRequestWrapper {

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