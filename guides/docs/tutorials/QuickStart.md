# Snitch Quick Start Guide

This guide will help you quickly set up a basic web service using Snitch.

## Installation

Add Snitch to your project dependencies:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:1.0.0")
}
```

## Hello World Example

Create a simple "Hello World" service:

```kotlin
import snitch.gson.GsonJsonParser
import snitch.dsl.snitch
import snitch.dsl.routes
import snitch.dsl.response.ok

fun main() {
    snitch(GsonJsonParser)
        .onRoutes {
            GET("hello") isHandledBy { "world".ok }
        }
        .start()
        .serveDocumenation()
}
```

This creates a service that:
- Responds with "world" when you make a GET request to `/hello`
- Automatically generates API documentation available at `/docs`

## Creating a RESTful API

Let's create a more realistic example with multiple endpoints:

```kotlin
import snitch.gson.GsonJsonParser
import snitch.dsl.*
import snitch.dsl.response.*

// Define our data classes
data class User(val id: String, val name: String, val email: String)
data class CreateUserRequest(val name: String, val email: String)

// In-memory storage for this example
val users = mutableMapOf<String, User>()

fun main() {
    snitch(GsonJsonParser)
        .onRoutes {
            "users" / {
                // GET /users - List all users
                GET() isHandledBy { 
                    users.values.toList().ok 
                }
                
                // POST /users - Create a new user
                POST() with body<CreateUserRequest>() isHandledBy { 
                    val id = java.util.UUID.randomUUID().toString()
                    val user = User(id, body.name, body.email)
                    users[id] = user
                    user.created
                }
                
                // GET /users/{userId} - Get a specific user
                userId / {
                    GET() isHandledBy {
                        val id = request[userId]
                        users[id]?.ok ?: "User not found".notFound()
                    }
                    
                    // DELETE /users/{userId} - Delete a user
                    DELETE() isHandledBy {
                        val id = request[userId]
                        if (users.containsKey(id)) {
                            users.remove(id)
                            "User deleted".ok
                        } else {
                            "User not found".notFound()
                        }
                    }
                }
            }
        }
        .start()
        .serveDocumenation()
}

// Define a path parameter
val userId by path()
```

## Parameter Validation

Let's enhance our API with parameter validation:

```kotlin
// Define validated parameters
val limit by query(ofNonNegativeInt(max = 30, default = 10))
val offset by query(ofNonNegativeInt(default = 0))
val email by query(ofEmail)

// Define our own custom validator
val ofEmail = stringValidator("valid email") { 
    it.contains("@") && it.contains(".") 
}

// Use in routes
"users" / {
    // GET /users?limit=10&offset=0
    GET() with listOf(limit, offset) isHandledBy {
        users.values
            .toList()
            .drop(request[offset])
            .take(request[limit])
            .ok
    }
    
    // GET /users/search?email=user@example.com
    "search" / {
        GET() with email isHandledBy {
            val searchEmail = request[email]
            users.values
                .filter { it.email == searchEmail }
                .toList()
                .ok
        }
    }
}
```

## Adding Middleware

Implement a simple logging middleware:

```kotlin
val Router.log get() = decorating {
    println("➡️ ${request.method} ${request.path} - Request started")
    val response = next()
    println("⬅️ ${request.method} ${request.path} - Response: ${response.statusCode}")
    response
}

// Apply middleware to routes
routes {
    log {
        "users" / {
            // All user routes will be logged
            GET() isHandledBy { users.values.toList().ok }
            // ...
        }
    }
}
```

## Authentication

Implement a basic authentication system:

```kotlin
// Authentication middleware
val Router.authenticated get() = decorating {
    val authHeader = request.headerParams("Authorization")
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
        val token = authHeader.substring(7)
        if (isValidToken(token)) {
            next() // Proceed to the handler
        } else {
            "Invalid token".unauthorized()
        }
    } else {
        "Authentication required".unauthorized()
    }
}

// Apply to protected routes
routes {
    "public" / {
        // Public endpoints...
    }
    
    "api" / {
        authenticated {
            // Protected endpoints...
            "profile" / {
                GET() isHandledBy { getCurrentUser().ok }
            }
        }
    }
}
```

## Using Conditions

Implement access control with conditions:

```kotlin
// Define conditions
val isAdmin = condition("isAdmin") {
    val user = getCurrentUser()
    if (user.role == "ADMIN") {
        ConditionResult.Successful
    } else {
        ConditionResult.Failed("Admin access required".forbidden())
    }
}

// Apply conditions to endpoints
"admin" / {
    // This endpoint requires admin privileges
    GET("dashboard") onlyIf isAdmin isHandledBy { 
        getAdminDashboard().ok 
    }
}
```

## Testing Your API

Test your endpoints with the built-in testing DSL:

```kotlin
class UserApiTest : SnitchTest({ port -> setupApp(port) }) {
    
    @Test
    fun `get all users returns 200`() {
        GET("/users")
            .expectCode(200)
            .expectBodyContains("[]") // Initially empty
    }
    
    @Test
    fun `create user returns 201`() {
        POST("/users")
            .withBody("""{"name":"John","email":"john@example.com"}""")
            .expectCode(201)
            .expectBodyContains("John")
    }
}
```

## Next Steps

Now that you have a basic understanding of Snitch, explore:

1. **Documentation Generation**: Learn how to enhance your API documentation
2. **Error Handling**: Implement global exception handlers
3. **Custom Validators**: Create complex validation rules
4. **Coroutines**: Use Kotlin coroutines for asynchronous operations

For more details, check out:
- [Snitch Documentation](../Docs.md)
- [In Depth Guides](../in%20depth/)