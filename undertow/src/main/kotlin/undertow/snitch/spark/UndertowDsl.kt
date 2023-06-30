package undertow.snitch.spark

import io.undertow.server.HttpServerExchange
import io.undertow.util.PathTemplateMatch
import me.snitch.parameters.HeaderParameter
import me.snitch.parameters.Parameter
import me.snitch.parameters.PathParam
import me.snitch.parameters.QueryParameter
import me.snitch.parsing.Parser
import me.snitch.request.RequestWrapper
import me.snitch.request.filterValid
import me.snitch.service.DecoratedWrapper
import me.snitch.types.HTTPMethods
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
