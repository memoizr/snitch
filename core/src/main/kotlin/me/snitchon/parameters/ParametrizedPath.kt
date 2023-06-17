package me.snitchon.parameters

data class ParametrizedPath(val path: String, val pathParameters: Set<PathParam<out Any, out Any>>) {
    operator fun div(path: String) = copy(path = this.path + "/" + path)
    operator fun div(path: PathParam<out Any, out Any>) =
        copy(path = this.path + "/" + "{${path.name}}", pathParameters = pathParameters + path)
}