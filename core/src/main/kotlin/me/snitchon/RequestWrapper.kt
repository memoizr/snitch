package me.snitchon

interface RequestWrapper {
    val body: () -> Any?
    fun params(name: String): String?
    fun headers(name: String): String?
    fun queryParams(name: String): String?

    fun method(): HTTPMethod


    fun getPathParam(param: PathParam<*, *>): String?
    fun getQueryParam(param: QueryParameter<*, *>): String?
    fun getHeaderParam(param: HeaderParameter<*, *>): String?


    fun missingParameterMessage(path: String, it: Parameter<*, *>) =
        """Required $path parameter `${it.name}` is missing"""

    fun invalidParameterMessage(query: String, it: Parameter<*, *>, value: String?) =
        """$query parameter `${it.name}` is invalid, expecting ${it.pattern.description}, got `$value`"""

    fun getInvalidParams(
        pathParams: Set<PathParam<out Any, *>>,
        queryParams: Set<QueryParameter<*, *>>,
        headerParams: Set<HeaderParameter<*, *>>,
    ): List<String> {
        return (pathParams.map { validateParam(it, getPathParam(it), "Path") } +
                queryParams.map { validateParam(it, getQueryParam(it), "Query") } +
                headerParams.map { validateParam(it, getHeaderParam(it), "Header") })
            .filterNotNull()
    }

    fun validateParam(it: Parameter<*, *>, value: String?, path: String): String? {
        return when {
            it.required && value == null -> missingParameterMessage(path, it)
            !it.required && value == null -> null
            it.pattern.regex.matches(value.toString()) -> null
            else -> {
                invalidParameterMessage(path, it, value)
            }
        }
    }
}