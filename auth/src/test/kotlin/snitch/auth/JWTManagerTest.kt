package snitch.auth

import com.memoizr.assertk.expect
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import snitch.shank.resetShank
import snitch.tests.InlineSnitchTest
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JWTManagerTest {
    private val jwtManager = JWTManager()
    private val fixedTime = Instant.parse("2023-01-01T12:00:00Z")
    private val fixedClock = Clock.fixed(fixedTime, ZoneId.systemDefault())
    
    @BeforeEach
    fun setup() {
        // Override the clock to use a fixed time for predictable tests
        SecurityModule.clock.override { -> fixedClock }
    }
    
    @AfterEach
    fun tearDown() {
        // Restore the original clock
//        SecurityModule.clock.restore()
        resetShank()
    }
    
    @Test
    fun `newToken should create a valid JWT token with correct claims`() {
        // Arrange
        val claims = JWTClaims("testUser", Role.USER)
        
        // Act
        val token = jwtManager.newToken(claims)
        
        // Assert
        assertTrue(token.isNotBlank(), "Token should not be blank")
        
        // Validate the token to ensure it contains the correct claims
        val result = jwtManager.validate(token)
        assertIs<Authentication.Authenticated>(result, "Token validation should return Authenticated")
        
        val authenticatedResult = result as Authentication.Authenticated
        assertEquals("testUser", authenticatedResult.claims.userId, "User ID should match")
        assertEquals(Role.USER, authenticatedResult.claims.role, "Role should match")
    }
    
    @Test
    fun `validate should return Authenticated for valid token`() {
        // Arrange
        val claims = JWTClaims("testUser", Role.USER)
        val token = jwtManager.newToken(claims)
        
        // Act
        val result = jwtManager.validate(token)
        
        // Assert
        assertIs<Authentication.Authenticated>(result, "Should return Authenticated for valid token")
        val authenticatedResult = result as Authentication.Authenticated
        assertEquals("testUser", authenticatedResult.claims.userId, "User ID should match")
        assertEquals(Role.USER, authenticatedResult.claims.role, "Role should match")
    }

    @Test
    fun `validate should return InvalidToken for malformed token`() {
        // Arrange
        val malformedToken = "eyJhbGciOiJSUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInVzZXJJZCI6InVzZXIxMjMifQ" // Incomplete token
        
        // Act
        val result = jwtManager.validate(malformedToken)
        
        // Assert
        assertIs<Authentication.InvalidToken>(result, "Should return InvalidToken for malformed token")
    }
    
    @Test
    fun `validate should return MissingToken for empty token`() {
        // Act
        val result = jwtManager.validate("")
        
        // Assert
        assertIs<Authentication.MissingToken>(result, "Should return MissingToken for empty token")
    }

    @Test
    fun `tokens should have different signatures for different users`() {
        // Arrange
        val user1Claims = JWTClaims("user1", Role.USER)
        val user2Claims = JWTClaims("user2", Role.USER)
        
        // Act
        val token1 = jwtManager.newToken(user1Claims)
        val token2 = jwtManager.newToken(user2Claims)
        
        // Assert
        assertTrue(token1 != token2, "Tokens for different users should be different")
    }
    
    @Test
    fun `tokens should have different signatures for different roles`() {
        // Arrange
        val userClaims = JWTClaims("testUser", Role.USER)
        val adminClaims = JWTClaims("testUser", Role.ADMIN)
        
        // Act
        val userToken = jwtManager.newToken(userClaims)
        val adminToken = jwtManager.newToken(adminClaims)
        
        // Assert
        assertTrue(userToken != adminToken, "Tokens for different roles should be different")
    }
}

class JWTManagerIntegrationTest : InlineSnitchTest({ _, port -> testRoutes("", this)(port) }) {
    private val fixedTime = Instant.parse("2023-01-01T12:00:00Z")
    private val fixedClock = Clock.fixed(fixedTime, ZoneId.systemDefault())
    
    // Generate tokens for different scenarios
    val userToken = SecurityModule.jwt().newToken(JWTClaims("testUser", Role.USER))
    val expiredToken = generateExpiredToken()
    val malformedToken = "eyJhbGciOiJSUzI1NiJ9.eyJyb2xlIjoiVVNFUiIsInVzZXJJZCI6InVzZXIxMjMifQ" // Incomplete token
    
    private fun generateExpiredToken(): String {
        // Store the original clock
        val originalClock = SecurityModule.clock()
        
        try {
            // Override the clock to return a time in the past
            val pastTime = Instant.now().minus(20, ChronoUnit.MINUTES)
            SecurityModule.clock.override { -> java.time.Clock.fixed(pastTime, java.time.ZoneId.systemDefault()) }
            
            // Generate a token with the past time (which will be expired)
            return SecurityModule.jwt().newToken(JWTClaims("expired789", Role.USER))
        } finally {
            // Restore the original clock
//            SecurityModule.clock.restore()
        }
    }
    
    @BeforeEach
    fun setup() {
        // Override the clock to use a fixed time for predictable tests
        SecurityModule.clock.override { -> fixedClock }
    }
    
    @AfterEach
    fun tearDown() {
        // Restore the original clock
//        SecurityModule.clock.restore()
        resetShank()
    }
    
    @Test
    fun `JWT authentication should work in a real request-response cycle`() {
        given {
            authenticated {
                GET("protected") isHandledBy { "Protected content for ${request.principal}".ok }
            }
        } then {
            // Valid token should authenticate and allow access
            GET("/protected")
                .withHeaders(mapOf("X-Access-Token" to userToken))
                .expect {
                    expect that it.statusCode() isEqualTo 200
                    expect that it.body() contains "Protected content for testUser"
                }
                
            // Expired token should return unauthorized
            // TODO fix this
//            GET("/protected")
//                .withHeaders(mapOf("X-Access-Token" to expiredToken))
//                .expect {
//                    expect that it.statusCode() isEqualTo 401
//                    expect that it.body() contains "unauthorized"
//                }
                
            // Malformed token should return unauthorized
            GET("/protected")
                .withHeaders(mapOf("X-Access-Token" to malformedToken))
                .expect {
                    expect that it.statusCode() isEqualTo 401
                    expect that it.body() contains "unauthorized"
                }
                
            // Missing token should return 400
            GET("/protected")
                .expect {
                    expect that it.statusCode() isEqualTo 400
                    expect that it.body() contains "missing"
                }
        }
    }
}