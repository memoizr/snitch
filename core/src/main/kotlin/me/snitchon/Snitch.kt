package me.snitchon

import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
//import spark.Service
import java.io.File

//class Snitch(val config: Config = Config()) {
//    val http by lazy { Service.ignite().port(config.port) }
//
//
//    fun setRoutes(routerConfiguration: Router.() -> Unit): Router {
//        val tmpDir = File(System.getProperty("java.io.tmpdir") + "/swagger-ui/docs")
//        if (!tmpDir.exists()) {
//            tmpDir.mkdirs()
//        }
//        http.externalStaticFileLocation(tmpDir.absolutePath)
//        val logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
//        logger.level = config.logLevel
//
//        val router = Router(config, http)
//        routerConfiguration(router)
//        router.startListening()
//        return router
//    }
//
//    fun Router.startListening() {
//        endpoints.forEach {
//            val path = config.basePath + it.endpoint.url.replace("/{", "/:").replace("}", "")
//            when (it.endpoint.httpMethod) {
//                HTTPMethod.GET -> service.get(path, it.function)
//                HTTPMethod.POST -> service.post(path, it.function)
//                HTTPMethod.PUT -> service.put(path, it.function)
//                HTTPMethod.PATCH -> service.patch(path, it.function)
//                HTTPMethod.HEAD -> service.head(path, it.function)
//                HTTPMethod.DELETE -> service.delete(path, it.function)
//                HTTPMethod.OPTIONS -> service.options(path, it.function)
//            }
//        }
//    }
//
//    fun stop() {
//        http.stop()
//    }
//}