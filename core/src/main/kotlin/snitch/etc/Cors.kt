package snitch.etc

import snitch.response.HttpResponse
import snitch.router.Router
import snitch.router.Routes
import snitch.router.plus
import snitch.router.transformEndpoints
import snitch.types.StatusCodes

val cors = options + corsHeaders

val corsHeaders
    get(): Router.(Routes) -> Router = {
        transformEndpoints {
            decorated {
                next().cors(setOf(this.wrap.request.method.name))
            }
        }(this, it)
        this
    }

val options
    get(): Router.(Routes) -> Router = {
        transformEndpoints {
            decorated {
                next().cors(setOf(this.wrap.request.method.name))
            }
        }(this, it)
        endpoints.map { it.endpoint }
            .groupBy { it.path }
            .onEach {
                val methods = it.value.map { it.httpMethod.name }.toSet()
                OPTIONS(it.key) isHandledBy { "".ok.cors(methods) }
            }
        this
    }

fun <T, S : StatusCodes> HttpResponse<T, S>.cors(
    methods: Set<String>,
    allowOrigin: String = "*",
    allowHeader: String = "*",
    allowCredentials: Boolean = true,
    maxAge: String = "86400",
) = this
    .header("Access-Control-Allow-Methods" to methods.joinToString())
    .header("Access-Control-Allow-Origin" to allowOrigin)
    .header("Access-Control-Allow-Headers" to allowHeader)
    .header("Access-Control-Allow-Credentials" to "$allowCredentials")
    .header("Access-Control-Max-Age" to maxAge)
