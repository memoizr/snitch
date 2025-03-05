package snitch

import snitch.service.RoutedService
import snitch.tests.SnitchTest

abstract class BaseTest(service: (Int) -> RoutedService) : SnitchTest(service) {
}