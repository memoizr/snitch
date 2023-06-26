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
Although there are a few built in validator-transformers, they offer a relatively weak typing. Best practice involves transforming and validating raw platform types into domain types. For example a `userId` is rarely actually just a string, for example it's unlikely the content of `Shakespeare.txt` parsed as string could possibly be a valid ID for a user. You most likely have a `value class UserId` defined somewhere. Likewise a search filter is usually something like an `Enum` where you have a set of pre-determined filter values. 

Defining custom validator-transformers in snitch is simple:
```kotlin
value class UserId(val id: UUID)
enum class Filter { EXPIRED, ACTIVE, CANCELLED, PENDING }

val ofUserId = stringValidator("the ID of the user") { UserId(UUID.fromString(it)) }

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

`limit` and `offset` here are defined so that if these parameters were not provided, or provided incorrectly, a default value would be provided instead. This is in case a "fail quietly" behaviour is desired. By default a `fail explicitly` behaviour is supported, so empty or invalid inputs will return a 400 to inform the API user they're probably doing something wrong.

