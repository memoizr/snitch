package snitch.auth

import com.memoizr.assertk.expect
import org.junit.jupiter.api.Test
import snitch.parameters.path
import snitch.tests.InlineSnitchTest

class DecorationsTest : InlineSnitchTest({ _, port -> testRoutes("", this)(port) }) {
    // Generate tokens for different roles and scenarios
    val userToken = SecurityModule.jwt().newToken(JWTClaims("testUser", Role.USER))
    val adminToken = SecurityModule.jwt().newToken(JWTClaims("admin456", Role.ADMIN))
    val invalidToken = "invalid.token.format"
    
    // Path parameter for testing principalEquals condition
    val userId by path()
    
    @Test
    fun `authenticated decorator should allow authenticated requests`() {
        given {
            GET("public") isHandledBy { "public content".ok }
            
            authenticated {
                GET("protected") isHandledBy { "protected content".ok }
            }
        } then {
            // Public endpoint should be accessible without authentication
            GET("/public")
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "public content"
                }
            
            // Protected endpoint should be accessible with valid token
            GET("/protected")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "protected content"
                }
        }
    }

    @Test
    fun `principal extension should return user ID from authenticated request`() {
        given {
            authenticated {
                GET("whoami") isHandledBy { request.principal.ok }
            }
        } then {
            // Should return the principal (user ID) from the token
            GET("/whoami")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "testUser"
                }
        }
    }
    
    @Test
    fun `role extension should return role from authenticated request`() {
        given {
            authenticated {
                GET("myrole") isHandledBy { request.role.name.ok }
            }
        } then {
            // Should return the role from the token
            GET("/myrole")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "USER"
                }
                
            GET("/myrole")
                .withHeaders(mapOf("X-Access-Token" to adminToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "ADMIN"
                }
        }
    }
    
    @Test
    fun `principalEquals condition should return Successful when principal matches parameter`() {
        given {
            authenticated {
                GET("users" / userId) onlyIf principalEquals(userId) isHandledBy { 
                    "Profile for ${request[userId]}".ok
                }
            }
        } then {
            // User can access their own profile
            GET("/users/testUser")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "Profile for testUser"
                }
        }
    }
    
    @Test
    fun `principalEquals condition should return Failed when principal does not match parameter`() {
        given {
            authenticated {
                GET("users" / userId) onlyIf principalEquals(userId) isHandledBy { 
                    "Profile for $userId".ok 
                }
            }
        } then {
            // User cannot access another user's profile
            GET("/users/admin456")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 403
                    expect that it.body() contains "forbidden"
                }
        }
    }
    
    @Test
    fun `FORBIDDEN response should be returned for forbidden access`() {
        given {
            authenticated {
                GET("admin-only") onlyIf hasAdminRole isHandledBy { 
                    "Admin content".ok 
                }
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
}
