package snitch.example

import org.junit.jupiter.api.Test

class HealthTest : BaseTest() {
    @Test
    fun `performs liveness test`() {
        GET("/health/liveness")
            .expectCode(200)
    }
}
