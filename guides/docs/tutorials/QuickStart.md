# Snitch Quick Start Guide

This guide will help you quickly set up a basic web service using Snitch.

## Installation

Add Snitch to your project dependencies:

```kotlin
dependencies {
    implementation("io.github.memoizr:snitch-bootstrap:4.0.0")
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
// Simple logging middleware that doesn't require parameters
val Router.log get() = decorateWith {
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
// Define the header parameter for authentication
val accessToken by header(
    condition = validAccessToken,
    name = "Authorization",
    description = "Bearer token for authentication"
)

// Validator for access token
val validAccessToken = stringValidator { token ->
    if (token.startsWith("Bearer ")) {
        val actualToken = token.substring(7)
        if (isValidToken(actualToken)) {
            Authentication.Authenticated(JWTClaims(getUserId(actualToken), getRole(actualToken)))
        } else {
            Authentication.InvalidToken
        }
    } else {
        Authentication.MissingToken
    }
}

// Authentication result model
sealed interface Authentication {
    data class Authenticated(val claims: JWTClaims) : Authentication
    interface Unauthenticated : Authentication
    object InvalidToken : Unauthenticated
    object MissingToken : Unauthenticated
}

// Data class for JWT claims
data class JWTClaims(val userId: UserId, val role: Role)
data class UserId(val value: String)
enum class Role { USER, ADMIN }

// Authentication middleware with proper parameter declaration
val Router.authenticated get() = decorateWith(accessToken) {
    when (val auth = request[accessToken]) {
        is Authentication.Authenticated -> {
            next() // Proceed to the handler
        }
        is Authentication.Unauthenticated -> "Authentication required".unauthorized()
    }
}

// Extension properties to access authentication data
val RequestWrapper.principal: UserId get() = 
    (request[accessToken] as Authentication.Authenticated).claims.userId
val RequestWrapper.role: Role get() = 
    (request[accessToken] as Authentication.Authenticated).claims.role

// Apply to protected routes
routes {
    "public" / {
        // Public endpoints...
    }
    
    "api" / {
        authenticated {
            // Protected endpoints...
            "profile" / {
                GET() isHandledBy { 
                    getUserProfile(request.principal).ok 
                }
            }
            
            // Example of using principal in a handler
            "posts" / {
                GET() isHandledBy { 
                    getPostsByUser(request.principal).ok 
                }
                
                POST() with body<CreatePostRequest>() isHandledBy {
                    createPost(request.principal, body.title, body.content).created
                }
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
    if (request.role == Role.ADMIN) {
        ConditionResult.Successful
    } else {
        ConditionResult.Failed("Admin access required".forbidden())
    }
}

// Condition to check if the user is the owner of a resource
fun isOwner(resourceIdParam: Parameter<String, *>) = condition("isOwner") {
    val resourceId = request[resourceIdParam]
    val resource = getResourceById(resourceId)
    
    if (resource?.ownerId == request.principal.value) {
        ConditionResult.Successful
    } else {
        ConditionResult.Failed("You don't have permission to access this resource".forbidden())
    }
}

// Apply conditions to endpoints
routes {
    authenticated {
        // Admin-only endpoint
        "admin" / {
            GET("dashboard") onlyIf isAdmin isHandledBy { 
                getAdminDashboard().ok 
            }
        }
        
        // User can only access their own posts
        "posts" / postId / {
            GET() onlyIf isOwner(postId) isHandledBy { getPost() }
            PUT() onlyIf isOwner(postId) with body<UpdatePostRequest>() isHandledBy { updatePost() }
            DELETE() onlyIf isOwner(postId) isHandledBy { deletePost() }
        }
    }
}
```

## Handler Functions

Snitch provides a clean way to define handler functions that can access the request context:

```kotlin
// Define a path parameter
val postId by path()

// Handler for getting a post
private val getPost by handling {
    postsRepository().getPost(PostId(request[postId]))
        ?.toResponse?.ok
        ?: "Post not found".notFound()
}

// Handler for deleting a post
private val deletePost by handling {
    postsRepository().deletePost(request.principal, PostId(request[postId]))
        .noContent
}

// Handler for getting all posts for the current user
private val getPosts by handling {
    postsRepository().getPosts(request.principal)
        .toResponse.ok
}

// Handler with request body parsing
private val createPost by parsing<CreatePostRequest>() handling {
    postsRepository().putPost(
        CreatePostAction(
            request.principal,
            PostTitle(body.title),
            PostContent(body.content),
        )
    ).mapSuccess {
        SuccessfulCreation(value).created
    }.mapFailure {
        FailedCreation().badRequest()
    }
}

// Usage in routes
routes {
    authenticated {
        "posts" / {
            GET() isHandledBy getPosts
            POST() with body<CreatePostRequest>() isHandledBy createPost
            
            postId / {
                GET() isHandledBy getPost
                DELETE() isHandledBy deletePost
            }
        }
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