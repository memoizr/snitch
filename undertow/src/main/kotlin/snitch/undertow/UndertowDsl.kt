package snitch.undertow

import io.undertow.server.HttpServerExchange
import io.undertow.util.PathTemplateMatch
import snitch.parameters.HeaderParameter
import snitch.parameters.Parameter
import snitch.parameters.PathParam
import snitch.parameters.QueryParameter
import snitch.request.RequestWrapper
import snitch.request.filterValid
import snitch.service.DecoratedWrapper
import snitch.types.HTTPMethods
import snitch.types.Parser
import java.net.URLDecoder


class UndertowRequestWrapper(
    override val parser: Parser,
    override val params: Set<Parameter<*, *>>,
    val exchange: HttpServerExchange,
    val _body: () -> Any?,
) : RequestWrapper {
    override val body: () -> Any? get() = _body
    override val method get() = HTTPMethods.fromString(exchange.requestMethod.toString())
    override val path: String get() = exchange.requestPath

    override fun params(name: String): String? =
        URLDecoder.decode(exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY).parameters.get(name))

    override fun headers(name: String): Collection<String> = exchange.requestHeaders.get(name).orEmpty()

    override fun queryParams(name: String): Collection<String> = exchange.queryParameters.get(name).orEmpty()

    override fun getPathParam(param: PathParam<*, *>): String? =
        params(param.name).filterValid(param)

    override fun getQueryParam(param: QueryParameter<*, *>) =
        queryParams(param.name).filterValid(param)

    override fun getHeaderParam(param: HeaderParameter<*, *>) =
        headers(param.name).filterValid(param)
}

val RequestWrapper.undertow get() = this as UndertowRequestWrapper
val DecoratedWrapper.undertow get() = this.wrap as UndertowRequestWrapper
