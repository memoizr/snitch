package snitch

import snitch.tests.SnitchTest
import org.junit.After
import org.junit.Before
import snitch.service.RoutedService

abstract class BaseTest(service: (Int) -> RoutedService) : SnitchTest(service) {

    @Before
    override fun before() {
        super.before()
    }

    @After
    override fun after() {
        super.after()
    }
}