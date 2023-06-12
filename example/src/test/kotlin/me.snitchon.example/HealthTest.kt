package me.snitchon.example

import me.snitchon.tests.SnitchTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HealthTest : BaseTest() {
    @Test
    fun `performs liveness test`() {
        GET("/health/liveness")
            .expectCode(200)
    }
}

abstract class BaseTest() : SnitchTest({ Application.start(it) }) {
    @BeforeEach
    override fun before() {
        super.before()
    }

    @AfterEach
    override fun after() {
        super.after()
    }
}