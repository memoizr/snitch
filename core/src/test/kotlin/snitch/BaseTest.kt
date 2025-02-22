package snitch

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import snitch.service.RoutedService
import snitch.tests.SnitchTest

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest(service: (Int) -> RoutedService) : SnitchTest(service) {

    @BeforeAll
    fun beforeAll() {
        super.before()
    }

    @AfterAll
    fun afterAll() {
        super.after()
    }
}