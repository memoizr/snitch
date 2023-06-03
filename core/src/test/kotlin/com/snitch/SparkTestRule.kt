package com.snitch

import ch.qos.logback.classic.Level
import com.snitch.spark.SparkSnitchService
import me.snitchon.parsers.GsonJsonParser
import org.junit.rules.ExternalResource
import org.junit.runner.Description
import org.junit.runners.model.Statement
import java.net.BindException
import java.net.ConnectException

val config = Config(description = "A test",
        basePath = "/$root",
        title = "Tunemoji API Documentation",
        port = 3000,
        logLevel = Level.INFO,
        host = "http://localhost:3000/$root",
        docPath = "spec",
        docExpansion = DocExpansion.LIST
)

open class SparkTestRule(port: Int, val router: Router.() -> Unit = ServerRouter) : ExternalResource() {
    val server = SparkSnitchService(config.copy(port = port), GsonJsonParser)

    override fun apply(base: Statement, description: Description): Statement {
        return object : Statement() {
            override fun evaluate() {
                before()
                fun go() {
                    try {
                        base.evaluate()
                    } catch (b: BindException) {
                        go()
                    } catch (e: ConnectException) {
                        go()
                    } finally {
                        after()
                    }
                }
                go()

            }
        }
    }

    override fun before() {
        server.setRoutes(router)
//        server.http.awaitInitialization()
    }
}
