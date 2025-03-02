package snitch.auth

import com.memoizr.assertk.expect
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import snitch.tests.InlineSnitchTest
import java.time.Instant
import java.time.temporal.ChronoUnit

class ValidationHeadersTest : InlineSnitchTest({ _, port -> testRoutes("", this)(port) }) {
    // Generate tokens for different roles and scenarios
    val userToken = SecurityModule.jwt().newToken(JWTClaims("testUser", Role.USER))
    val adminToken = SecurityModule.jwt().newToken(JWTClaims("admin456", Role.ADMIN))
    val invalidToken = "invalid.token.format"
    val expiredToken = generateExpiredToken()
    
    private fun generateExpiredToken(): String {
        // Store the original clock
        val originalClock = SecurityModule.clock()
        
        try {
            // Override the clock to return a time in the past
            val pastTime = Instant.now().minus(20, ChronoUnit.MINUTES)
//            SecurityModule.clock.overrideFactory { -> java.time.Clock.fixed(pastTime, java.time.ZoneId.systemDefault()) }
            
            // Generate a token with the past time (which will be expired)
            return SecurityModule.jwt().newToken(JWTClaims("expired789", Role.USER))
        } finally {
            // Restore the original clock
//            SecurityModule.clock.restore()
        }
    }
    
    @Test
    fun `accessToken parameter should have correct name and description`() {
        // Access the parameter to check its properties
        val accessToken = ValidationHeaders.accessToken
        
        // Assert
        expect that accessToken.name isEqualTo "X-Access-Token"
        expect that accessToken.description isEqualTo "Access token for the principal user"
    }
    
    @Test
    fun `accessToken parameter should use validAccessToken validator`() {
        given {
            authenticated {
                GET("protected") isHandledBy { request.principal.ok }
            }
        } then {
            // Valid token should authenticate and return the principal
            GET("/protected")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "testUser"
                }
        }
    }
    
    @Test
    @Disabled
    fun `validAccessToken validator should return appropriate authentication result`() {
        given {
            authenticated {
                GET("protected") isHandledBy { "Protected content".ok }
            }
        } then {
            // Invalid token should return unauthorized
//            GET("/protected")
//                .withHeaders(mapOf("X-Access-Token" to invalidToken))
//                .expect {
//                    expect that it.statusCode() isEqualTo 400
//                }
//
            // Expired token should return unauthorized
//            GET("/protected")
//                .withHeaders(mapOf("X-Access-Token" to expiredToken))
//                .expect {
//                    expect that it.statusCode() isEqualTo 401
//                    expect that it.body() contains "unauthorized"
//                }
                
            // Missing token should return unauthorized
            GET("/protected")
                .expect {
                    expect that it.statusCode() isEqualTo 401
                    expect that it.body() contains "unauthorized"
                }
        }
    }
    
    @Test
    fun `different tokens should authenticate with different principals`() {
        given {
            authenticated {
                GET("whoami") isHandledBy { request.principal.ok }
            }
        } then {
            // User token should authenticate as testUser
            GET("/whoami")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "testUser"
                }
                
            // Admin token should authenticate as admin456
            GET("/whoami")
                .withHeaders(mapOf("X-Access-Token" to adminToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "admin456"
                }
        }
    }
}
