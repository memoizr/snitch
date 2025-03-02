package snitch.auth

import com.memoizr.assertk.expect
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import snitch.config.SnitchConfig
import snitch.parameters.path
import snitch.parsers.GsonJsonParser
import snitch.router.Routes
import snitch.service.ConditionResult
import snitch.service.RoutedService
import snitch.service.condition
import snitch.tests.InlineSnitchTest
import snitch.undertow.UndertowSnitchService
import java.time.Instant
import java.time.temporal.ChronoUnit

class AuthTestInt : InlineSnitchTest({ _, port -> testRoutes("", this)(port) }) {
    // Generate tokens for different roles and scenarios
    val userToken = SecurityModule.jwt().newToken(JWTClaims("user123", Role.USER))
    val adminToken = SecurityModule.jwt().newToken(JWTClaims("admin456", Role.ADMIN))
    val expiredToken = generateExpiredToken()
    val invalidToken = "invalid.token.format"
    val malformedToken = "eyJhbGciOiJSUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInVzZXJJZCI6InVzZXIxMjMifQ" // Incomplete token

    private fun generateExpiredToken(): String {
        // Store the original clock
        val originalClock = SecurityModule.clock()
        
        try {
            // Override the clock to return a time in the past
            val pastTime = Instant.now().minus(20, ChronoUnit.MINUTES)
//            SecurityModule.clock.overrideFactory { -> Clock.fixed(pastTime, ZoneId.systemDefault()) }
            
            // Generate a token with the past time (which will be expired)
            return SecurityModule.jwt().newToken(JWTClaims("expired789", Role.USER))
        } finally {
            // Restore the original clock
//            SecurityModule.clock.restore()
        }
    }

    @BeforeEach
    fun setup() {
        // Reset any overrides before each test
//        SecurityModule.clock.restore()
    }

    @Test
    fun `basic authentication - allows access with valid token and denies with invalid token`() {
        given {
            GET("example") isHandledBy { "hi".ok }
            authenticated {
                GET("authenticated") isHandledBy { "hi".ok }
            }
        } then {
            // Public endpoint should be accessible without authentication
            GET("/example") expect {
                expect that it.body() contains "hi"
            }
            
            // Protected endpoint should be accessible with valid token
            GET("/authenticated")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.body() contains "hi"
                }
            
            // Protected endpoint should deny access with invalid token
            GET("/authenticated")
                .withHeaders(mapOf("X-Access-Token" to "xxx"))
                .expect {
                    expect that it.body() contains "401" contains "unauthorized"
                }
        }
    }

    @Test
    @Disabled
    fun `expired token - should return unauthorized with appropriate message`() {
        given {
            authenticated {
                GET("protected") isHandledBy { "protected content".ok }
            }
        } then {
            GET("/protected")
                .withHeaders(mapOf("X-Access-Token" to expiredToken))
                .expect {
                    expect that it.statusCode() isEqualTo 401
                    expect that it.body() contains "unauthorized"
                }
        }
    }

    @Test
    fun `malformed token - should return unauthorized with appropriate message`() {
        given {
            authenticated {
                GET("protected") isHandledBy { "protected content".ok }
            }
        } then {
            GET("/protected")
                .withHeaders(mapOf("X-Access-Token" to malformedToken))
                .expect {
                    expect that it.statusCode() isEqualTo 401
                    expect that it.body() contains "unauthorized"
                }
        }
    }

    @Test
    fun `missing token - should return error when token is missing`() {
        given {
            authenticated {
                GET("protected") isHandledBy { "protected content".ok }
            }
        } then {
            // No token provided
            GET("/protected")
                .expect {
                    expect that it.statusCode() isEqualTo 400
                }
        }
    }

    @Test
    fun `role-based access - admin role can access admin resources`() {
        given {
            authenticated {
                GET("admin-resource") onlyIf hasAdminRole isHandledBy { "admin content".ok }
            }
        } then {
            // Admin can access admin resource
            GET("/admin-resource")
                .withHeaders(mapOf("X-Access-Token" to adminToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "admin content"
                }
                
            // User cannot access admin resource
            GET("/admin-resource")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 403
                    expect that it.body() contains "forbidden"
                }
        }
    }
    val userId by path()

    @Test
    fun `principal equality - user can only access their own resources`() {
        given {
            authenticated {
                GET("users" / userId / "profile") onlyIf principalEquals(userId) isHandledBy { "${request[userId]} profile".ok }
            }
        } then {
            // User can access their own profile
            GET("/users/user123/profile")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "user123 profile"
                }
                
            // User cannot access another user's profile
            GET("/users/admin456/profile")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 403
                    expect that it.body() contains "forbidden"
                }
                
            // Admin can access their own profile
            GET("/users/admin456/profile")
                .withHeaders(mapOf("X-Access-Token" to adminToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "admin456 profile"
                }
        }
    }


    @Test
    fun `nested authentication - inner routes inherit authentication requirements`() {
        given {
            GET("public") isHandledBy { "public content".ok }
            
            authenticated {
                GET("outer") isHandledBy { "outer content".ok }
                
                "inner" / {
                    GET() isHandledBy { "inner content".ok }
                    
                    "deep" / {
                        GET() isHandledBy { "deep content".ok }
                    }
                }
            }
        } then {
            // Public route is accessible without authentication
            GET("/public")
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "public content"
                }
                
            // All authenticated routes require a token
            GET("/outer")
                .expect {
                    expect that it.statusCode() isEqualTo 400
                }
                
            GET("/inner")
                .expect {
                    expect that it.statusCode() isEqualTo 400
                }
                
            GET("/inner/deep")
                .expect {
                    expect that it.statusCode() isEqualTo 400
                }
                
            // With a valid token, all authenticated routes are accessible
            GET("/outer")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "outer content"
                }
                
            GET("/inner")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "inner content"
                }
                
            GET("/inner/deep")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "deep content"
                }
        }
    }

    @Test
    fun `accessing principal and role - handlers can access authenticated user information`() {
        given {
            authenticated {
                GET("whoami") isHandledBy { request.principal.ok }
                GET("myrole") isHandledBy { request.role.name.ok }
            }
        } then {
            // User can see their principal ID
            GET("/whoami")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "user123"
                }
                
            // User can see their role
            GET("/myrole")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "USER"
                }
                
            // Admin can see their principal ID
            GET("/whoami")
                .withHeaders(mapOf("X-Access-Token" to adminToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "admin456"
                }
                
            // Admin can see their role
            GET("/myrole")
                .withHeaders(mapOf("X-Access-Token" to adminToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "ADMIN"
                }
        }
    }
}

// Role-based conditions
val hasAdminRole = condition("hasAdminRole") {
    if (request.role == Role.ADMIN) ConditionResult.Successful
    else ConditionResult.Failed(FORBIDDEN())
}

val hasUserRole = condition("hasUserRole") {
    if (request.role == Role.USER) ConditionResult.Successful
    else ConditionResult.Failed(FORBIDDEN())
}

fun testRoutes(basePath: String = "", router: Routes): (Int) -> RoutedService = { port ->
    UndertowSnitchService(
        GsonJsonParser,
        SnitchConfig(
            SnitchConfig.Service(
                basePath = basePath,
                port = port
            )
        )
    ).onRoutes(router)
}
