package snitch

import com.memoizr.assertk.expect
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import snitch.config.loadConfigFromFile
import snitch.parsers.GsonJsonParser
import snitch.tests.SnitchTest
import snitch.undertow.UndertowSnitchService

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigurationTest : SnitchTest({
    UndertowSnitchService(
        GsonJsonParser,
        loadConfigFromFile("configuration.yml")
    ).onRoutes {
        GET("customport").isHandledBy { "3333".ok }
    }
}) {
    override val port = 3333

    @BeforeAll
    fun beforeAll() {
        super.before()
    }

    @AfterAll
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

        val parsed = snitch.config.parse(config)

        expect that parsed isEqualTo """
            service:
               port: 3000
               foo: foo
               bar: bar_me
               baz: baz
            """.trimIndent()
    }

}
