package me.snitchon.example

import io.mockk.mockk
import io.mockk.verify
import me.snitchon.example.ApplicationModule.logger
import org.junit.jupiter.api.Test

class LogginTest : BaseTest() {

    @Test
    fun `it logs before and after the request is made`() {
        logger.override { mockk(relaxed = true) }

        GET("/health/liveness")
            .expectCode(200)

        verify { logger().info("Begin Request: GET /health/liveness") }
        verify { logger().info("""End Request: GET /health/liveness 200 "ok"""") }
    }
}