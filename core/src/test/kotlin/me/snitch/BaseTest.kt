package me.snitch

import me.snitch.service.RoutedService
import me.snitch.tests.SnitchTest
import org.junit.After
import org.junit.Before

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