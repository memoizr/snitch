package snitch.dsl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import snitch.parameters.header
import snitch.router.decorateWith
import snitch.router.plus

/**
 * This test class validates the examples from the "Mastering Snitch Decorations" tutorial.
 * Each test corresponds to a specific example or concept from the tutorial.
 */
class DecorationTutorialTest : InlineSnitchTest() {

    // Mock logger
    private object Logger {
        val logs = mutableListOf<String>()
        
        fun info(message: String) {
            logs.add("[INFO] $message")
        }
        
        fun error(message: String) {
            logs.add("[ERROR] $message")
        }
        
        fun clear() {
            logs.clear()
        }
    }
    
    // Mock objects for the examples
    private val metrics = mutableMapOf<String, Long>()
    private val requestCounts = mutableMapOf<String, Int>()
    private val cache = mutableMapOf<String, String>()
    private val featureFlags = mutableMapOf(
        "new-ui" to true,
        "experimental" to false
    )
    
    // Mock user and role for auth examples
    enum class Role { USER, ADMIN }
    data class User(val id: String, val role: Role)
    data class Authentication(val userId: String, val role: Role) {
        companion object {
            val Authenticated = Authentication("user1", Role.USER)
            val Unauthenticated = null
        }
    }

    @Test
    fun `basic decoration usage - logging`() {
        Logger.clear()
        
        val logged = decorateWith {
            val method = request.method.name
            val path = request.path
            Logger.info("Begin Request: $method $path")
            next().also {
                Logger.info("End Request: $method $path ${it.statusCode.code}")
            }
        }

        given {
            logged {
                GET("hello").isHandledBy { "Hello, world!".ok }
            }
        } then {
            GET("/hello").expectBody(""""Hello, world!"""")
            
            assertEquals(2, Logger.logs.size)
            assertTrue(Logger.logs[0].contains("Begin Request: GET /hello"))
            assertTrue(Logger.logs[1].contains("End Request: GET /hello 200"))
        }
    }

    @Test
    fun `transaction management decoration`() {
        var transactionStarted = false
        var transactionEnded = false
        
        // Mock transaction function
        fun <T> transaction(block: () -> T): T {
            transactionStarted = true
            val result = block()
            transactionEnded = true
            return result
        }
        
        val withTransaction = decorateWith { 
            transaction { 
                next() 
            } 
        }

        given {
            withTransaction {
                GET("users").isHandledBy { "Users list".ok }
            }
        } then {
            GET("/users").expectBody(""""Users list"""")
            
            assertTrue(transactionStarted)
            assertTrue(transactionEnded)
        }
    }

    @Test
    fun `parameterized decoration - with metric label`() {
        val recordedMetrics = mutableMapOf<String, Long>()
        
        fun withMetricLabel(label: String) = decorateWith {
            val startTime = System.currentTimeMillis()
            val response = next()
            val endTime = System.currentTimeMillis()
            recordedMetrics[label] = endTime - startTime
            response
        }

        given {
            withMetricLabel("user-service")() {
                GET("users").isHandledBy { "Users list".ok }
            }
        } then {
            GET("/users").expectBody(""""Users list"""")
            
            assertTrue(recordedMetrics.containsKey("user-service"))
        }
    }

    @Test
    fun `decoration composition - combining multiple decorations`() {
        Logger.clear()
        val executionOrder = mutableListOf<String>()
        
        val logged = decorateWith {
            executionOrder.add("logged - before")
            Logger.info("Begin Request")
            val response = next()
            Logger.info("End Request")
            executionOrder.add("logged - after")
            response
        }
        
        var transactionStarted = false
        var transactionEnded = false
        
        // Mock transaction function
        fun <T> transaction(block: () -> T): T {
            transactionStarted = true
            executionOrder.add("transaction - start")
            val result = block()
            executionOrder.add("transaction - end")
            transactionEnded = true
            return result
        }
        
        val withTransaction = decorateWith { 
            transaction { 
                next() 
            } 
        }
        
        val combinedDecoration = withTransaction + logged

        given {
            combinedDecoration {
                GET("users").isHandledBy { 
                    executionOrder.add("handler")
                    "Users list".ok 
                }
            }
        } then {
            GET("/users").expectBody(""""Users list"""")
            
            // Verify execution order: right-to-left for request, left-to-right for response
            assertEquals("transaction - start", executionOrder[0])
            assertEquals("logged - before", executionOrder[1])
            assertEquals("handler", executionOrder[2])
            assertEquals("logged - after", executionOrder[3])
            assertEquals("transaction - end", executionOrder[4])
            
            assertTrue(transactionStarted)
            assertTrue(transactionEnded)
        }
    }

    @Test
    fun `decoration nesting - applying decorations in layers`() {
        val executionOrder = mutableListOf<String>()
        
        val logged = decorateWith {
            executionOrder.add("logged - before")
            val response = next()
            executionOrder.add("logged - after")
            response
        }
        
        var transactionStarted = false
        var transactionEnded = false
        
        // Mock transaction function
        fun <T> transaction(block: () -> T): T {
            transactionStarted = true
            executionOrder.add("transaction - start")
            val result = block()
            executionOrder.add("transaction - end")
            transactionEnded = true
            return result
        }
        
        val withTransaction = decorateWith { 
            transaction { 
                next() 
            } 
        }

        given {
            logged {
                withTransaction {
                    GET("nested").isHandledBy { 
                        executionOrder.add("handler")
                        "Nested".ok 
                    }
                }
            }
        } then {
            GET("/nested").expectBody(""""Nested"""")
            
            // Verify execution order: outside-in for request, inside-out for response
            assertEquals("logged - before", executionOrder[0])
            assertEquals("transaction - start", executionOrder[1])
            assertEquals("handler", executionOrder[2])
            assertEquals("transaction - end", executionOrder[3])
            assertEquals("logged - after", executionOrder[4])
        }
    }

    @Test
    fun `complex decoration flow - composition and nesting`() {
        val executionOrder = mutableListOf<String>()
        
        val decoration1 = decorateWith {
            executionOrder.add("decoration1 - before")
            val response = next()
            executionOrder.add("decoration1 - after")
            response
        }
        
        val decoration2 = decorateWith {
            executionOrder.add("decoration2 - before")
            val response = next()
            executionOrder.add("decoration2 - after")
            response
        }
        
        val decoration3 = decorateWith {
            executionOrder.add("decoration3 - before")
            val response = next()
            executionOrder.add("decoration3 - after")
            response
        }

        given {
            // Composition
            // note the reversed order of application
            (decoration2 + decoration1) {
                // Nesting
                decoration3 {
                    GET("complex").isHandledBy { 
                        executionOrder.add("handler")
                        "Complex".ok 
                    }
                }
            }
        } then {
            GET("/complex").expectBody(""""Complex"""")
            
            // Verify execution order
            assertEquals("decoration2 - before", executionOrder[0])
            assertEquals("decoration1 - before", executionOrder[1])
            assertEquals("decoration3 - before", executionOrder[2])
            assertEquals("handler", executionOrder[3])
            assertEquals("decoration3 - after", executionOrder[4])
            assertEquals("decoration1 - after", executionOrder[5])
            assertEquals("decoration2 - after", executionOrder[6])
        }
    }

    // Define auth token parameter
    val authToken by header("X-Auth-Token")
    @Test
    fun `real-world example - authentication and authorization`() {
        val executionOrder = mutableListOf<String>()
        

        // Authentication decoration using proper parameter access
        val authenticated = decorateWith(authToken) {
            when (request[authToken]) {
                "user-token" -> {
                    executionOrder.add("auth - user authenticated")
                    next()
                }
                "admin-token" -> {
                    executionOrder.add("auth - admin authenticated") 
                    next()
                }
                else -> {
                    executionOrder.add("auth - unauthorized")
                    "Unauthorized".unauthorized()
                }
            }
        }
        
        // Role-based authorization decoration
        val requireAdmin = decorateWith(authToken) {
            val token = request[authToken]
            
            if (token == "admin-token") {
                executionOrder.add("admin check - allowed")
                next()
            } else {
                executionOrder.add("admin check - forbidden")
                "Forbidden - Admin access required".forbidden()
            }
        }

        given {
            authenticated {
                // Public endpoint - just needs authentication
                GET("profile").isHandledBy { 
                    executionOrder.add("handler - profile")
                    "User profile".ok 
                }

                // Admin endpoint - needs both authentication and admin role
                requireAdmin {
                    GET("admin/dashboard").isHandledBy {
                        executionOrder.add("handler - admin dashboard")
                        "Admin dashboard".ok
                    }
                }
            }
            endpoints.map {it.endpoint}.forEach {
                println(it.headerParams)
            }
        } then {
            // Test unauthorized access
            GET("/profile")
                .expectCode(400)
                .expectBody("""{"statusCode":400,"details":["Required Header parameter `X-Auth-Token` is missing"]}""")
            

            // Test authenticated user access to profile
            GET("/profile")
                .withHeader("X-Auth-Token" to "user-token")
                .expectBody(""""User profile"""")

            // Test regular user access to admin area
            GET("/admin/dashboard")
                .withHeader("X-Auth-Token" to "user-token")
                .expectCode(403)
                .expectBody(""""Forbidden - Admin access required"""")

            // Test admin access to admin area
            GET("/admin/dashboard")
                .withHeader("X-Auth-Token" to "admin-token")
                .expectBody(""""Admin dashboard"""")
        }
    }

    @Test
    fun `error handling decoration`() {
        Logger.clear()
        
        val handleErrors = decorateWith {
            try {
                next()
            } catch (e: Exception) {
                Logger.error("Error handling request: ${e.message}")
                "Internal server error".serverError()
            }
        }

        given {
            handleErrors {
                GET("success").isHandledBy { "Success".ok }
                GET("error").isHandledBy { 
                    throw RuntimeException("Simulated error")
                    "Never reached".ok
                }
            }
        } then {
            GET("/success").expectBody(""""Success"""")
            
            GET("/error")
                .expectCode(500)
                .expectBody(""""Internal server error"""")
            
            assertTrue(Logger.logs.any { it.contains("Error handling request: Simulated error") })
        }
    }

    // Mock rate limiter
//    object RateLimiter {
//        fun getRequestCount(clientIp: String, timeWindow: Duration): Int {
//            return requestCounts.getOrDefault(clientIp, 0)
//        }
//
//        fun incrementRequestCount(clientIp: String) {
//            requestCounts[clientIp] = getRequestCount(clientIp, Duration.ZERO) + 1
//        }
//    }

//    @Test
//    fun `rate limiting decoration`() {
//        val requestCounts = mutableMapOf<String, Int>()
//
//        fun rateLimit(maxRequests: Int, perTimeWindow: Duration) = decorateWith {
//            val clientIp = request.remoteAddress
//            val requestCount = RateLimiter.getRequestCount(clientIp, perTimeWindow)
//
//            if (requestCount >= maxRequests) {
//                return@decorateWith "Rate limit exceeded. Try again later.".error(429)
//            }
//
//            RateLimiter.incrementRequestCount(clientIp)
//            next()
//        }
//
//        given {
//            rateLimit(2, Duration.ofMinutes(1))() {
//                GET("api/messages").isHandledBy { "Messages".ok }
//            }
//        } then {
//            // First request - should succeed
//            GET("/api/messages").expectBody(""""Messages"""")
//
//            // Second request - should succeed
//            GET("/api/messages").expectBody(""""Messages"""")
//
//            // Third request - should be rate limited
//            GET("/api/messages")
//                .expectCode(429)
//                .expectBody(""""Rate limit exceeded. Try again later."""")
//        }
//    }

    // Mock cache service
//    object CacheService {
//        fun get(key: String): String? = cache[key]
//
//        fun put(key: String, value: String, ttl: Duration) {
//            cache[key] = value
//        }
//    }

//    @Test
//    fun `caching decoration`() {
//        val cache = mutableMapOf<String, String>()
//        var handlerCallCount = 0
//
//
//        fun cache(ttl: Duration) = decorateWith {
//            val cacheKey = "${request.method.name}-${request.path}"
//            val cachedResponse = CacheService.get(cacheKey)
//
//            if (cachedResponse != null) {
//                return@decorateWith cachedResponse.ok
//            }
//
//            val response = next()
//            CacheService.put(cacheKey, response.value(), ttl)
//            response
//        }
//
//        given {
//            cache(Duration.ofMinutes(5))() {
//                GET("api/products").isHandledBy {
//                    handlerCallCount++
//                    "Products ${handlerCallCount}".ok
//                }
//            }
//        } then {
//            // First request - should call handler
//            GET("/api/products").expectBody(""""Products 1"""")
//
//            // Second request - should use cache
//            GET("/api/products").expectBody(""""Products 1"""")
//
//            // Verify handler was only called once
//            assertEquals(1, handlerCallCount)
//        }
//    }

    @Test
    fun `real-world example - logging with transaction management`() {
        Logger.clear()
        
        val logged = decorateWith {
            val method = request.method.name
            val path = request.path
            Logger.info("Begin Request: $method $path")
            next().also {
                Logger.info("End Request: $method $path ${it.statusCode.code}")
            }
        }
        
        var transactionStarted = false
        var transactionEnded = false
        
        // Mock transaction function
        fun <T> transaction(block: () -> T): T {
            transactionStarted = true
            val result = block()
            transactionEnded = true
            return result
        }
        
        val withTransaction = decorateWith { 
            transaction { 
                next() 
            } 
        }

        given {
            logged {
                withTransaction {
                    GET("users").isHandledBy { "Users list".ok }
                    POST("users").isHandledBy { "User created".created }
                }
            }
        } then {
            GET("/users").expectBody(""""Users list"""")
            
            assertTrue(transactionStarted)
            assertTrue(transactionEnded)
            assertEquals(2, Logger.logs.size)
            assertTrue(Logger.logs[0].contains("Begin Request: GET /users"))
            assertTrue(Logger.logs[1].contains("End Request: GET /users 200"))
        }
    }
} 