package snitch.tests

import snitch.router.Routes
import snitch.service.RoutedService
import java.util.*

abstract class InlineSnitchTest(private val router: Routes.(String, Int) -> RoutedService) : Ported, TestMethods {
    override open val port = Random().nextInt(5000) + 2000

    data class RoutedTest(val routedService: RoutedService) {
        fun assert(assertionBlock: () -> Unit) {
            routedService.start()
            try {
                assertionBlock()
            } finally {
                routedService.stop()
            }
        }
    }

    infix fun RoutedService.then(assertionBlock: () -> Unit) {
        this.start()
        try {
            assertionBlock()
        } finally {
            this.stop()
        }
    }

    fun given(routes: Routes) = router(routes, "", port)//testRoutes("", routes)(port)
}