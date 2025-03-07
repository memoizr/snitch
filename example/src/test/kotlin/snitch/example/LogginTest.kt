package snitch.example

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import snitch.example.ApplicationModule.logger
import snitch.example.database.DBModule.connectionConfig
import snitch.example.database.DBModule.schema
import snitch.exposed.ExposedModule.connection
import snitch.exposed.testing.ExposedConfig
import snitch.exposed.testing.ExposedTest

class LogginTest : BaseTest(), ExposedTest {
    override val exposedConfig = ExposedConfig(
        connection(connectionConfig()), schema(),
    )

    @Test
    fun `it logs before and after the request is made`() {
        logger.override { mockk(relaxed = true) }

        GET("/health/liveness")
            .expectCode(200)

        verify { logger().info("Begin Request: GET /health/liveness") }
        verify { logger().info("""End Request: GET /health/liveness 200 "ok"""") }
    }
}