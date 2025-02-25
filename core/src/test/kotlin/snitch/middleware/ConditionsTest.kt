package snitch.middleware

import org.junit.jupiter.api.Test
import snitch.dsl.InlineSnitchTest
import snitch.parameters.optionalQuery
import snitch.parameters.path
import snitch.parameters.query
import snitch.service.ConditionResult
import snitch.service.condition
import snitch.types.StatusCodes

class ConditionsTest : InlineSnitchTest() {
    // Parameters
    val q by query()
    val p by path()
    val optional by optionalQuery()

    // Conditions
    val hasQueryTrue = condition("hasQueryTrue") {
        when (request[q]) {
            "true" -> ConditionResult.Successful
            else -> ConditionResult.Failed("".forbidden())
        }
    }

    val pathIsY = condition("pathIsY") {
        when (request[p]) {
            "y" -> ConditionResult.Successful
            else -> ConditionResult.Failed("".forbidden())
        }
    }

    val alwaysTrue = condition("alwaysTrue") {
        ConditionResult.Successful
    }

    val alwaysFalse = condition("alwaysFalse") {
        ConditionResult.Failed("".forbidden())
    }

    val customMessage = condition("customMessage") {
        ConditionResult.Failed("Custom error message".error(StatusCodes.IMA_TEAPOT)) // I'm a teapot status
    }

    val optionalParamExists = condition("optionalParamExists") {
        if (request[optional] != null) ConditionResult.Successful
        else ConditionResult.Failed("Optional param missing".forbidden())
    }

    @Test
    fun `supports conditions`() {
        given {
            GET("foo" / p)
                .with(listOf(q))
                .onlyIf(hasQueryTrue and pathIsY)
                .isHandledBy { "".ok }
        } then {
            // Only the request with both conditions satisfied should succeed
            GET("/foo/y?q=true").expectCode(200)

            // All other combinations should fail
            GET("/foo/x?q=true").expectCode(403)  // path is not 'y'
            GET("/foo/y?q=false").expectCode(403) // query is not 'true'
            GET("/foo/x?q=false").expectCode(403) // neither condition is met
        }
    }

    @Test
    fun `supports OR conditions`() {
        given {
            GET("bar" / p)
                .with(listOf(q))
                .onlyIf(hasQueryTrue or pathIsY)
                .isHandledBy { "".ok }
        } then {
            // Any request with at least one condition satisfied should succeed
            GET("/bar/y?q=true").expectCode(200)  // both conditions met
            GET("/bar/x?q=true").expectCode(200)  // only query condition met
            GET("/bar/y?q=false").expectCode(200) // only path condition met

            // Only fails when neither condition is met
            GET("/bar/x?q=false").expectCode(403)
        }
    }

    @Test
    fun `supports NOT conditions`() {
        given {
            GET("not")
                .onlyIf(!alwaysFalse)
                .isHandledBy { "".ok }
        } then {
            GET("/not").expectCode(200)
        }

        given {
            GET("not-true")
                .onlyIf(!alwaysTrue)
                .isHandledBy { "".ok }
        } then {
            GET("/not-true").expectCode(400)
        }
    }

    @Test
    fun `supports complex condition combinations`() {
        given {
            GET("complex")
                .with(listOf(q, optional))
                .onlyIf((hasQueryTrue and pathIsY) or (alwaysTrue and !alwaysFalse))
                .isHandledBy { "".ok }
        } then {
            // Should succeed because (alwaysTrue and !alwaysFalse) is true
            GET("/complex?q=false").expectCode(200)
        }
    }

    @Test
    fun `supports custom error responses`() {
        given {
            GET("teapot")
                .onlyIf(customMessage)
                .isHandledBy { "".ok }
        } then {
            GET("/teapot").expectCode(418)
        }
    }

    @Test
    fun `handles optional parameters in conditions`() {
        given {
            GET("optional")
                .with(listOf(optional))
                .onlyIf(optionalParamExists)
                .isHandledBy { "".ok }
        } then {
            GET("/optional?optional=value").expectCode(200)
            GET("/optional").expectCode(403)
        }
    }

    @Test
    fun `short-circuits condition evaluation`() {
        var secondConditionEvaluated = false

        val trackingCondition = condition("tracking") {
            secondConditionEvaluated = true
            ConditionResult.Successful
        }

        given {
            GET("short-circuit")
                .onlyIf(alwaysFalse and trackingCondition)
                .isHandledBy { "".ok }
        } then {
            GET("/short-circuit").expectCode(403)
            assert(!secondConditionEvaluated) { "Second condition should not have been evaluated" }
        }
    }
}