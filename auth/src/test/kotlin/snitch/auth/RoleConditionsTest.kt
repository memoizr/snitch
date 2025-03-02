package snitch.auth

import com.memoizr.assertk.expect
import org.junit.jupiter.api.Test
import snitch.parameters.path
import snitch.service.ConditionResult.Failed
import snitch.service.ConditionResult.Successful
import snitch.service.condition
import snitch.tests.InlineSnitchTest

class RoleConditionsTest : InlineSnitchTest({ _, port -> testRoutes("", this)(port) }) {
    // Generate tokens for different roles and scenarios
    val userToken = SecurityModule.jwt().newToken(JWTClaims("user123", Role.USER))
    val adminToken = SecurityModule.jwt().newToken(JWTClaims("admin456", Role.ADMIN))

    // Path parameter for testing principalEquals condition
    val userId by path()
    
    // Define role-based conditions for testing
    private val hasAdminRole = condition("hasAdminRole") {
        if (request.role == Role.ADMIN) Successful
        else Failed(FORBIDDEN())
    }

    private val hasUserRole = condition("hasUserRole") {
        if (request.role == Role.USER) Successful
        else Failed(FORBIDDEN())
    }
    
    @Test
    fun `hasAdminRole should return Successful for admin users`() {
        given {
            authenticated {
                GET("admin-only") onlyIf hasAdminRole isHandledBy { "Admin content".ok }
            }
        } then {
            // Admin can access admin-only content
            GET("/admin-only")
                .withHeaders(mapOf("X-Access-Token" to adminToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "Admin content"
                }
        }
    }
    
    @Test
    fun `hasAdminRole should return Failed for non-admin users`() {
        given {
            authenticated {
                GET("admin-only") onlyIf hasAdminRole isHandledBy { "Admin content".ok }
            }
        } then {
            // Regular user cannot access admin-only content
            GET("/admin-only")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 403
                    expect that it.body() contains "forbidden"
                }
        }
    }
    
    @Test
    fun `hasUserRole should return Successful for user role`() {
        given {
            authenticated {
                GET("user-only") onlyIf hasUserRole isHandledBy { "User content".ok }
            }
        } then {
            // Regular user can access user-only content
            GET("/user-only")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "User content"
                }
        }
    }
    
    @Test
    fun `hasUserRole should return Failed for admin users`() {
        given {
            authenticated {
                GET("user-only") onlyIf hasUserRole isHandledBy { "User content".ok }
            }
        } then {
            // Admin cannot access user-only content
            GET("/user-only")
                .withHeaders(mapOf("X-Access-Token" to adminToken))
                .expect {
                    expect that it.statusCode() isEqualTo 403
                    expect that it.body() contains "forbidden"
                }
        }
    }
}
