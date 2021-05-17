package com.snitch.spark

import com.snitch.*
import spark.Request
import spark.Response

class SparkRequestWrapper(val request: Request) : RequestWrapper {

    override val body: String by lazy { request.body() }

    override fun method(): HTTPMethod = HTTPMethod.fromString(request.requestMethod())

    override fun params(name: String): String? = request.params(name)

    override fun headers(name: String): String? = request.headers(name)

    override fun queryParams(name: String): String? = request.queryParams(name)

    override fun getPathParam(param: PathParam<*, *>): String? =
        request.params(param.name)
            .let { if (it != null && param.emptyAsMissing && it.isEmpty()) null else it }

    override fun getQueryParam(param: QueryParameter<*, *>) =
        request.queryParams(param.name).filterValid(param)

    override fun getHeaderParam(param: HeaderParameter<*, *>) =
        request.headers(param.name).filterValid(param)
}


class SparkResponseWrapper(val response: Response): ResponseWrapper {
    override fun setStatus(code: Int) = response.status(code)
    override fun setType(type: Format) = response.type(type.type)
}