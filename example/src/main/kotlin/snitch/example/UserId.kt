package snitch.example

import snitch.example.types.User
import snitch.parameters.header
import snitch.parameters.optionalQuery
import snitch.parameters.path
import snitch.parameters.query
import snitch.request.handling
import snitch.router.routes
import snitch.validation.ofNonEmptyString
import snitch.validation.ofNonNegativeInt
import snitch.validation.stringValidator
import java.util.*

// Domain types
data class UserId(val value: UUID)
data class PostId(val value: String)

// Validators
val ofUserId = stringValidator("valid user ID") { UserId(UUID.fromString(it)) }
val ofPostId = stringValidator("valid post ID") { PostId(it) }

class UserService {
    fun getUser(id: UserId) = User()
}
val userService = UserService()

// Parameters
val userId by path(ofUserId, description = "User identifier")
val postId by path(ofPostId, description = "Post identifier")
val includeComments by query(ofBoolean, description = "Whether to include comments")
val limit by optionalQuery(ofNonNegativeInt, default = 10, description = "Maximum results")
val apiVersion by header(ofNonEmptyString, description = "API version")

// Routes
val userController = routes {
    GET("users" / userId) withHeader apiVersion isHandledBy getUser
    
    GET("users" / userId / "posts") withQuery limit isHandledBy getUserPosts
    
    GET("posts" / postId) withQuery includeComments isHandledBy getPost
}

// Handlers
val getUser by handling {
    val user = userService.getUser(request[userId])
    user.ok
}

val getUserPosts by handling {
    val posts = postService.getUserPosts(
        userId = request[userId],
        limit = request[limit]
    )
    posts.ok
}

val getPost by handling {
    val post = postService.getPost(
        postId = request[postId],
        includeComments = request[includeComments]
    )
    post.ok
}