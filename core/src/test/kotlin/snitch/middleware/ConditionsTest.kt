package snitch.middleware

import org.junit.jupiter.api.Test
import snitch.dsl.InlineSnitchTest
import snitch.parameters.optionalQuery
import snitch.parameters.path
import snitch.parameters.query
import snitch.router.only
import snitch.service.ConditionResult
import snitch.service.condition
import snitch.types.StatusCodes

class ConditionsTest : InlineSnitchTest() {
    // Parameters
    val q by query()
    val p by path()
    val optional by optionalQuery()
    val age by query()
    val resourceId by path()
    val role by query()

    // Basic Conditions
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

    // Parameterized condition (from tutorial)
    fun hasMinimumAge(minAge: Int) = condition("hasMinimumAge($minAge)") {
        val userAge = request[age]?.toIntOrNull() ?: 0

        if (userAge >= minAge) {
            ConditionResult.Successful
        } else {
            ConditionResult.Failed("User must be at least $minAge years old".forbidden())
        }
    }

    // Role-based condition (from tutorial)
    val hasAdminRole = condition("hasAdminRole") {
        when (request[role]) {
            "ADMIN" -> ConditionResult.Successful
            else -> ConditionResult.Failed("Admin role required".forbidden())
        }
    }

    // Resource ownership condition (from tutorial)
    val isResourceOwner = condition("isResourceOwner") {
        if (request[resourceId] == "owned") ConditionResult.Successful
        else ConditionResult.Failed("Not the resource owner".forbidden())
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

    @Test
    fun `supports parameterized conditions`() {
        given {
            GET("age-check")
                .with(listOf(age))
                .onlyIf(hasMinimumAge(18))
                .isHandledBy { "".ok }
        } then {
            GET("/age-check?age=20").expectCode(200)  // Above minimum age
            GET("/age-check?age=18").expectCode(200)  // Exactly minimum age
            GET("/age-check?age=16").expectCode(403)  // Below minimum age
            GET("/age-check?age=invalid").expectCode(403)  // Invalid age
        }
    }

    @Test
    fun `supports condition hierarchies`() {
        given {
            GET("healthBefore").isHandledBy { "ok".ok }
            // Outer condition
            only(hasAdminRole) {
                GET("admin"/ "dashboard")
                    .with(listOf(role))
                    .isHandledBy { "admin dashboard".ok }

                // Inner condition
                only(isResourceOwner) {
                    GET("admin"/ "resources" / resourceId)
                        .with(listOf(role))
                        .isHandledBy { "admin resource".ok }
                }
            }
            GET("healthAfter").isHandledBy { "ok".ok }
        } then {
            // Admin can access dashboard
            GET("/healthBefore").expectCode(200)
            GET("/healthAfter").expectCode(200)
            GET("/admin/dashboard?role=ADMIN").expectCode(200)

            // Non-admin cannot access dashboard
            GET("/admin/dashboard?role=USER").expectCode(403)

            // Admin who owns the resource can access it
            GET("/admin/resources/owned?role=ADMIN").expectCode(200)

            // Admin who doesn't own the resource cannot access it
            GET("/admin/resources/not-owned?role=ADMIN").expectCode(403)

            // Non-admin cannot access resources regardless of ownership
            GET("/admin/resources/owned?role=USER").expectCode(403)
        }
    }

    @Test
    fun `supports complex authorization scenarios`() {
        given {
            GET("resource" / resourceId)
                .with(listOf(role))
                .onlyIf(isResourceOwner or hasAdminRole)
                .isHandledBy { "resource".ok }
        } then {
            // Resource owner can access
            GET("/resource/owned?role=USER").expectCode(200)

            // Admin can access even if not the owner
            GET("/resource/not-owned?role=ADMIN").expectCode(200)

            // Non-admin who doesn't own the resource cannot access
            GET("/resource/not-owned?role=USER").expectCode(403)
        }
    }

    @Test
    fun `supports condition negation in complex scenarios`() {
        val isResourceLocked = condition("isResourceLocked") {
            when (request[resourceId]) {
                "locked" -> ConditionResult.Successful
                else -> ConditionResult.Failed("Resource is not locked".forbidden())
            }
        }

        given {
            PUT("resource" / resourceId)
                .with(listOf(role))
                .onlyIf((isResourceOwner or hasAdminRole) and !isResourceLocked)
                .isHandledBy { "updated resource".ok }
        } then {
            // Owner can update unlocked resource
            PUT("/resource/owned?role=USER").expectCode(200)

            // Admin can update unlocked resource
            PUT("/resource/not-owned?role=ADMIN").expectCode(200)

            // Owner cannot update locked resource
            PUT("/resource/locked?role=USER").expectCode(403)

            // Admin cannot update locked resource
            PUT("/resource/locked?role=ADMIN").expectCode(400)

            // Non-owner, non-admin cannot update any resource
            PUT("/resource/not-owned?role=USER").expectCode(403)
        }
    }
}