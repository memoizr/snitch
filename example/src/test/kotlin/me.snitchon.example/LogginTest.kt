package me.snitchon.example

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import me.snitchon.example.ApplicationModule.clock
import me.snitchon.example.ApplicationModule.logger
import me.snitchon.example.ApplicationModule.now
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

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