package com.snitch

import com.memoizr.assertk.expect
import com.snitch.spark.UndertowSnitchService
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.response.ok
import me.snitchon.config.loadConfigFromFile
import me.snitchon.config.parse
import me.snitchon.tests.SnitchTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class ConfigurationTest : SnitchTest({
    UndertowSnitchService(
        GsonJsonParser,
        loadConfigFromFile("configuration.yml")
    ).setRoutes {
        GET("customport").isHandledBy { "3333".ok }
    }
}) {
    override val port = 3333

    @Before
    override fun before() {
        super.before()
    }

    @After
    override fun after() {
        super.after()
    }

    @Test
    fun `supports port from config`() {
        GET("/hey/customport")
            .expectCode(200)
            .expectBody(""""3333"""")
    }

    @Test
    fun `parses yml templates`() {

        val config = """
           service:
              port: {SERVER_PORT:3000}
              name: {SERVER_NAME}
              foo: foo
              bar: {BAR_ENV:bar}_{SUFFIX:me}
              baz: baz
           """.trimIndent()

        val parsed = parse(config)

        expect that parsed isEqualTo """
            service:
               port: 3000
               foo: foo
               bar: bar_me
               baz: baz
            """.trimIndent()
    }

}
