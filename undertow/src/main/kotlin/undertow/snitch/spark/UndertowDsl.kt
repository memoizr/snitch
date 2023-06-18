package undertow.snitch.spark

import io.undertow.server.HttpServerExchange
import io.undertow.util.PathTemplateMatch
import me.snitchon.parameters.HeaderParameter
import me.snitchon.parameters.Parameter
import me.snitchon.parameters.PathParam
import me.snitchon.parameters.QueryParameter
import me.snitchon.parsing.Parser
import me.snitchon.request.RequestWrapper
import me.snitchon.request.filterValid
import me.snitchon.service.DecoratedWrapper
import me.snitchon.types.HTTPMethods
import java.net.URLDecoder


class UndertowRequestWrapper(
    override val parser: Parser,
    override val params: Set<Parameter<*, *>>,
    val exchange: HttpServerExchange,
    inline val _body: () -> Any?,
) : RequestWrapper {
    override val body: () -> Any? get() = _body
    override val method get() = HTTPMethods.fromString(exchange.requestMethod.toString())
    override val path: String get() = exchange.requestPath

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

val RequestWrapper.undertow get() = this as UndertowRequestWrapper
val DecoratedWrapper.undertow get() = this.wrap as UndertowRequestWrapper
