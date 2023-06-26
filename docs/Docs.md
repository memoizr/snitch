# Snitch

#### Will snitch on your API. With swagger.

### Introduction

Snitch is a small and typesafe web framework for Kotlin

```kotlin
fun main() {
    snitch(GsonJsonParser).onRoutes {
        GET("hello") isHandledBy { "world".ok }
    }.start()
} 
```

#### Features

- Lightweight and fast.
- Functional approach
- OpenAPI 3 support
- Fully asynchronous execution
- Plain Kotlin. No reflection, code generation, annotation processing.
- Kotlin compiler is enough. No Gradle plugins

### Getting started 

```Kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.memoizr:snitch-bootstrap:3.2.7")
}
```
That's it, no need for command line tools, gradle plugins. It's just a simple library.


### Router
#### Routing basics
```kotlin
val root = routes {
    GET("foo") isHandledBy {
        "bar".ok
    }
    POST("foo") with body<FooRequest>() isHandledBy {
        "fooValue: ${body.fooValue}".created
    }
}
```        

The infix style is optional and a classic fluent approach is also supported.

```kotlin
val root = routes {
    GET("foo").isHandledBy {
        "bar".ok
    }
    POST("foo")
        .with(body<FooRequest>())
        .isHandledBy {
        "fooValue: ${body.fooValue}".created
    }
}
```        

Notice that `GET("/foo")` and `GET("foo")` are the same thing

You pass the router to the `onRoutes` function 
```kotlin
fun main() {
    snitch(GsonJsonParser).onRoutes(root).start()
} 
```

Of course in a real application you'd like to separate the route declarations from the endpoint implementations.

```kotlin
val root = routes {
    GET("foo") isHandledBy getFoo
    POST("foo") with body<FooRequest>() isHandledBy postFoo
}

val getFoo by handling {
    "bar".ok
}

val postFoo by parsing<FooRequest>() handling {
    "fooValue: ${body.fooValue}".created
}
```

#### Route Nesting
Services often have hundreds of routes, organized hierarchically. This can be modeled in Snitch:
```kotlin
val root = routes {
    "health" / healthController
    "users" / usersController
    "posts" / postsController
    ...
}

val usersController = routes {
    POST() with body<CreateUserRequest> isHandledBy createUser
    
    userId / {
        GET() isHandledBy getUser
        DELETE() isHandledBy deleteUser
        
        "posts" / {
            GET() isHandledBy getPosts
            POST() with body<CreatePostRequest> isHandledBy createPost
            postId / {
                GET() isHandledBy getPost
            }
        }
    }
}
```

This will define the following routes:
```
POST users
GET users/{userId}
DELETE users/{userId}
GET users/{userId}/posts
POST users/{userId}/posts
GET users/{userId}/posts/{postId}
```

Different teams however will have different styles that they endorse, so for those who would rather have a less DRY but more explicit route declaration, they can define the routes as:

```kotlin
val root = routes {
    healthController
    usersController
    postsController
    ...
}
val usersController = routes {
    POST("users") with body<CreateUserRequest> isHandledBy createUser
    
    GET("users" / userId) isHandledBy getUser
    DELETE("users" / userId) isHandledBy deleteUser

    GET("users" / userId / "posts") isHandledBy getPosts
    POST("users" / userId / "posts") with body<CreatePostRequest> isHandledBy createPost
    GET("users" / userId / "posts" / postId) isHandledBy getPost
}
```

The DSL is flexible so for teams that would like a measured and hybrid approach they can define the routes howerver they wish. For example grouping by path for all the actions supported on it:

```kotlin
val root = routes {
    healthController
    usersController
    postsController
    ...
}
val usersController = routes {
    "users" / {
        POST() with body<CreateUserRequest> isHandledBy createUser
    }
    "users" / userId / {
        GET() isHandledBy getUser
        DELETE() isHandledBy deleteUser
    }

    "users" / userId / "posts" / {
        GET() isHandledBy getPosts
        POST() with body<CreatePostRequest> isHandledBy createPost
    }

    "users" / userId / "posts" / postId / {
        GET() isHandledBy getPost
    }
}
```


#### HTTP input parameters

```kotlin
val userId by path()
val showDetails by query(ofBoolean)

val root = routes {
    GET("users" / userId / "profile")
        .with(showDetails)
        .isHandledBy {
            val user = UserId(request[userId])
            if (request[showDetails]) {
                usersRepository().profileDetails(user)
            } else {
                usersRepository().profileSummary(user)
            }.ok
        }
}
```        

Note: `userId` and `showDetails` are typed and validated. `request[showDetails]` will return a `Boolean` and `request[userId]` will return a `String`. If you don't pass a `Validator` such as `ofBoolean`, it defaults to `ofNonEmptyString`.  
Note: you have to declare the usage of a certain parameter in order to use it.

#### Input parameter validation and transformation
All parameters are validated and transformed to another type by default. Here's some more examples, let's add the type parameters explicitly so it's clear what's happening:

```Kotlin
val userId: String by path(nonEmptyString)  
val filters: Set<String> by path(ofNonEmptySet)
val showDetails: Boolean by path(ofBoolean)
```

#### Custom validations
Although there are a few built in validator-transformers, they offer a relatively weak typing. Best practice involves transforming and validating raw platform types into domain types. For example a `userId` is rarely actually just a string, for example it's unlikely the content of `Shakespeare.txt` parsed as string could possibly be a valid ID for a user. You most likely have a `value class UserId` defined somewhere. Likewise, a search filter is usually something like an `Enum` where you have a set of pre-determined filter values. 

Defining custom validator-transformers in snitch is simple:
```kotlin
value class UserId(val id: UUID)
enum class Filter { EXPIRED, ACTIVE, CANCELLED, PENDING }

val ofUserId = validator<String, UserId> { UserId(UUID.fromString(it)) }

// explicit types can be omitted for conciseness, here included for illustrative purposes
val userId: UserId by path(ofUserId)
val filters: Collection<Filter> by query(ofRepeatableEnum<Filter>())
val filter: Filter by query(ofEnum<Filter>())
```

> *Note:* Snitch is optimized for production code use cases, and in the spirit of Kotlin, it *enforces* best practices. In production, you almost always need to validate and transfrom inputs consistently. Snitch lets you do this in only one line of code in most cases, leading to a more concise, explicit and consistent codebase, making it easier to maintain larger codebases and for new developers to quickly become productive. 

#### Optional input parameters
Declaring a parameter with `query` or `header` will make it required. If the parameter is not supplied a `400` message will be returned specifying that that particular parameter was expected but not provided, as well as any other parameter that also does not pass validation. Optional parameters can be declared as such:


```kotlin
// request[sort] is nullable
val sort: Sorting? by optionalQuery(ofEnum<Sorting>())
```

The optionality functionality is quite powerful, offering a clear and consistent way of specifying default values as well as defining a behaviour for when these values are provided as empty as or as invalid inputs:

```kotlin
// request[sort] is not nullable, NEW is the default value
val sort: Sorting by optionalQuery(ofEnum<Sorting>(), default = NEW)

val limit: Int by optionalQuery(ofNonNegativeInt, default = 20, emptyAsMissing = true, invalidAsMissing = true)
val offset: Int by optionalQuery(ofNonNegativeInt, default = 0, emptyAsMissing = true, invalidAsMissing = true)
```

#### Parameter naming
Snitch aims at being as concise and as less verbose as possible while delivering a full feature set for production use-cases. In this spirit when you define an input parameter such as `val q by query()` it will create a named query parameter that should be supplied as such for example:`?q=urlencodedquery`. Note that the name of the parameter `val` in the codebase is by default the same name as in the API. If you want it to be different, it's simple:
```kotlin
val searchQuery by query(name = "searchQuery")
```

`limit` and `offset` here are defined so that if these parameters were not provided, or provided incorrectly, a default value would be provided instead. This is in case a "fail quietly" behaviour is desired. By default, a `fail explicitly` behaviour is supported, so empty or invalid inputs will return a 400 to inform the API user they're probably doing something wrong.

#### Unsafe, undocumented parameter parsing
While Snitch *enforces* best practices, leading to a less verbose and more consistent codebase that implements them, it also supports an *unsafe* traditional approach. If you want to access a parameter sneakily, and you don't care for the parameter to be included in the documentation, you can do it very simply with the cowboy-friendly syntax:

```kotlin
val getCows by handling {
    ...
    request.queryParams("numberOfCows")
    request.headerParams("ranch")
    request.pathParams("ranchId")
    ...
}
```
Although this approach is supported for niche use cases, it is strongly discouraged that this be used for most production applications unless there is a good reason for it.

#### Repeated parameters
In HTTP one of the hidden challenges to creating a robust and production grade API is that of handling the edge case of query or header parameters provided repeatedly when exactly one or at most one is expected. By default `val searchQuery by query()` expects exactly one value being provided and `val searchQuery by optionalQuery()` provides at most one semantics, unexpected repetition will result in 400. Support for repeated parameters can be made explicity by using `... by query(ofStringSet)` for example, which uses a repeatable validator. Custom validator for repeatable can be created in a very similar way to non-repeatable validators:

```kotlin
val ofUserId = repeatableValidator<String, UserId> { UserId(UUID.fromString(it)) }
```
#### Body parameter
Body parameters are treated differently than other input parameters as they are used in different ways. While it's common to share the same query parameters or headers between several endpoints (for example consider `limit`, `offset`, `orderBy`, `Access-Token` and so on), body parameters are often single use. Snitch aims at encouraging best  practices while reducing verbosity and clutter as much as possible, and in that spirit body parameter types are declared very simply:
```kotlin
POST("mypath") with body<MyRequest>() isHandleBy {
    // already parsed to MyRequest domain type
    request.body
}
```
This approach is typesafe, so if you were to omit the declaration of the body type, it would not be possible for you to access it within the handler:

```kotlin
POST("mypath") isHandleBy {
    // this resolves to Kotlin's Nothing special type and would not compile
    request.body
}
```
Binary path can also be supported inituitively by: `with(body<ByteArray>())`

### Middleware 
Snitch supports a very powerful and flexible middleware mechanism that can be used to implement a wide variety of features. Let's see how you can use it to create a simple logging behaviour applied to a route hierarchy:

```kotlin
val Router.log get() = decorating {
    logger().info("Begin Request: ${request.method.name} ${request.path}")
    next().also {
        logger().info("End Request: ${request.method.name} ${request.path} ${it.statusCode.code} ${it.value(parser)}")
    }
}

val rootRouter = routes {
    log {
        "health" / healthController
        "users" / usersController
    }
}
```

here `log` is a custom defined middleware logging behaviour. Its usage is very intuitive, and it's clear that such behaviour should be applied to any route defined within its block. Defining a new middleware is as straightforward as possible, here's the identity middleware, that simply calls the next action:
```kotlin
val Router.identity get() = using { next() }
```

the code block provided to `using` works similarly to the way handlers work, you can still access the request parameter in the same way with `request[myParam]` and can return responses with `ok` `created` `badRequest()` etc like in normal handlers.

Calling `next()` executes the code in the block of any nested middleware until it gets to the code block of the handler. `next()` returns the response from the next layer of the middleware and as such it can be transformed as appropriate. 

#### Order of execution
The order of execution, that is, what code is executed by the `next()` call, is dependent on the order of declaration. It works as your intuition would expect, inside out, from most nested to least nested:
```kotlin
//called second
log {
    // called first
    statistics {
        GET() ...
    }
}
```

### Security and Access Control
Middleware allows for the implementation of powerful and granular access control systems. Here's a realistic example:

```kotlin
val Router.authenticated
    get() = decorateEndpoints {
        with(listOf(accessToken)).decorate {
            when (request[accessToken]) {
                is Authentication.Authenticated -> next()
                is Authentication.Unauthenticated -> UNAUTHORIZED()
            }
        }
    }

val accessToken: Authentication by header(validAccessToken)

val validAccessToken = validator<String, Authentication> { jwt().validate(it) }

sealed interface Authentication {
    data class Authenticated(val claims: JWTClaims) : Authentication
    interface Unauthenticated : Authentication
    object InvalidToken : Unauthenticated
    object ExpiredToken : Unauthenticated
    object MissingToken : Unauthenticated
    object InvalidClaims : Unauthenticated
}
```

And this is how this is used
```kotlin

authenticated {
        ...
        GET(userId / "posts") isHandledBy getPosts
        ...
    }
}
```
Now, there's a lot to unpack in a few lines of code, let's break it down:
```kotlin
val Router.authenticated
    get() = decorateEndpoints {
        with(listOf(accessToken)).decorate {
            when (request[accessToken]) {
                is Authentication.Authenticated -> next()
                is Authentication.Unauthenticated -> UNAUTHORIZED()
            }
        }
    }
```

`decorateEndpoints` will apply whatever transformation inside the block to any endpoint to which this will be applied. `with(listOf(accessToken))` is declaring and adding the `accessToken` header parameter to the endpoints, documentation will reflect that. `request[accessToken]` parses, validates and transforms the access token provided in the headers. It returns a domain type, and we can proceed to the next layer of middleware in case the token is valid, and return a 401 error in case it is not.


### Guards
Still on top of the same underlying mechanism we've built a powerful and granular guard mechanism, here's an example of it at work:

```kotlin
val RequestWrapper.role: Role get() = (request[accessToken] as Authentication.Authenticated).claims.role

val hasAdminRole = condition {
    when (role) {
        ADMIN -> Successful()
        else -> Failed(FORBIDDEN())
    }
}
```
and this is how it's used
```kotlin
DELETE(postId) onlyIf hasAdminRole isHandledBy deletePost
```

`onlIf` takes a condition which can be either `Successful` or `Failed` and will either proceed with the request or terminate early accordingly.

This offers a high degree of granularity when specifying access control as applied to individual routes.

#### Composing conditions
Conditions are composable and support basic boolean logic operations:

```kotlin
DELETE(postId) onlyIf (principalEquals(userId) or hasAdminRole) isHandledBy deletePost
```

The code above hardly needs an explanation for what it's doing, despite the fact that it's not trivial behaviour.

here's the definition of `principalEquals`:
```kotlin
fun principalEquals(param: Parameter<out Any, *>) = condition {
    if (principal.value == request[param]) Successful()
    else Failed(FORBIDDEN())
}

val RequestWrapper.principal: UserId get() = (request[accessToken] as Authentication.Authenticated).claims.userId
```

#### Reusing conditions
Although it's possible to customize each and every endpoint to lock it down to the exact security guarantees your business logic needs to enforce, it's often the case that you need to share the same guard logic across several endpoints. Snitch offers two ways of doing this, the first one is obvious:

```kotlin
val ownerOrAdmin = principalEquals(userId) or hasAdminRole

DELETE(postId) onlyIf ownerOrAdmin isHandledBy deletePost
```

The second one is even more generic, as it can be applied to an entire sub-hierarchy of routes. It works similarly to how middleware does:

```kotlin
onlyIf(principalEquals(userId) or hasAdminRole) {
    ...
    DELETE(postId) isHandledBy deletePost
    PATCH(postId) with body<UpdatePostRequest>() isHandledBy updatePost
    ...
}
```

Note that this approach to Guards is in line with what we call "Snitch's way" or "Snitchy". Of course good old imperative checks inside the handler are still possible and supported, and in some cases that's the best thing to do. But sticking to Snitch's way allows for more consistent, readable and manageable codebases at any scale.

### Error handling
Although Snitch encourages a more functional approach to errors, it also supports global exception handling for both unexpected behaviour and for flow control.

```kotlin
snitch(GsonJsonParser)
    .onRoutes(root)
    .handleException(MyException::class) { exception ->
        MyCustomErroResponse(exception.reason)
            .also { logger().error(it.toString()) }
            .badRequest()
    }
    .start()
```

Note that the body of the exception handler works like the normal handlers', with the only difference that it has a referfence to the exception being handled, thie `it` of the lambda, which can be optionally named as in the example above. Note that it's not necessary to return an error response, it's possible to return an alternative successful response instead. You can see that there is a lot of functionality packed in a small amount of code, yet it still remains fairly intuitive and readable.

#### Polymorphic error handling
Note that error handling is polymorphic, so if `MyException` extends `MyBaseException` `.handleException(MyBaseException::class)...` would handle `MyException` as well as any other subclass of `MyBaseException`. For this reason, ordering of the declaration of exception handlers matters. You should always put the most specific handlers first, otherwise a more generic polymorphic handler would handle the exception instead. Note that this feature implementation relies on some reflection, and while it's relatively efficient, it's not as efficient as a more functional approach. For that reason this should not be used as a main flow control mechanism for performance critical applications.

