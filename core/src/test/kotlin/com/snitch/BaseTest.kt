package com.snitch

import me.snitchon.service.RoutedService
import me.snitchon.tests.SnitchTest
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