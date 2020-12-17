package com.snitch.spark

import com.snitch.*
import io.jooby.Context
import io.jooby.Value

class JoobyRequestWrapper(val context: Context) : RequestWrapper {
    override val body: String by lazy { context.body().value() }

    override fun method(): HTTPMethod = HTTPMethod.fromString(context.method)


    override fun params(name: String): String? = context.path(name).valueOrNull()

    override fun headers(name: String): String? = context.header(name).valueOrNull()

    override fun queryParams(name: String): String? = context.query(name).valueOrNull()

    override fun getPathParam(param: PathParam<*>): String? =
        context.path(param.name).valueOrNull()
            .let { if (it != null && param.emptyAsMissing && it.isEmpty()) null else it }

    override fun getQueryParam(param: QueryParameter<*>) =
        context.query(param.name).valueOrNull().filterValid(param)

    override fun getHeaderParam(param: HeaderParameter<*>) =
        context.header(param.name).valueOrNull().filterValid(param)
}


class JoobyResponseWrapper(val context: Context) : ResponseWrapper {
    override fun setStatus(code: Int) {
        context.setResponseCode(code)
    }

    override fun setType(type: Format) {
        context.setResponseType(type.type)
    }
}

fun Value.valueOrNull() = if (this.isMissing) null else value()