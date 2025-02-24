package snitch.middleware

import org.junit.jupiter.api.Test
import snitch.dsl.InlineSnitchTest
import snitch.parameters.path
import snitch.parameters.query
import snitch.service.ConditionResult
import snitch.service.condition

class ConditionsTest: InlineSnitchTest() {
    val q by query()
    val p by path()

    @Test
    fun `supports conditions`() {
        given {
            GET("foo"/p)
                .with(listOf(q))
                .onlyIf(myCond)
                .isHandledBy {"".ok}
        } then {
            GET("/foo/y?q=true").expectCode(200)
            GET("/foo/x?q=false").expectCode(403)
        }
    }

    val myCond = condition("myCond") {
        val x = if (request[q] == "true" && request[p] == "y") ConditionResult.Successful
        else ConditionResult.Failed("".forbidden())
        println(x)
        x
    }
}