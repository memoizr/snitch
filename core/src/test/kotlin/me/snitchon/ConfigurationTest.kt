package me.snitchon

import com.memoizr.assertk.expect
import undertow.snitch.spark.UndertowSnitchService
import me.snitchon.parsers.GsonJsonParser
import me.snitchon.config.loadConfigFromFile
import me.snitchon.config.parse
import me.snitchon.tests.SnitchTest
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

class ConfigurationTest : SnitchTest({
    UndertowSnitchService(
        GsonJsonParser,
        loadConfigFromFile("configuration.yml")
    ).onRoutes {
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
    @Ignore
    fun `supports port from config`() {
        GET("/hey/customport")
            .expectCode(200)
            .expectBody(""""3333"""")
    }

    @Test
    @Ignore
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
