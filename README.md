## Snitch

Snitch helps you create a production-grade HTTP layer for your applications and (micro)services with minimal effort. To create ***complete*** documentation for them with ***no*** effort.

Our primary goals are:
- To create the most readable and maintainable API for creating web services
- By readable we mean readable production code where routes are complex, parameters need validation, parsing and mapping to domain types, errors need to be handled, deal with authentication and permissions, etc.
- To fully automate the creation of documentation for your services 
- To enforce typesafe parameter parsing and validation
- To be as lightweight and performant as possible
- To not use any reflection, annotations or code generation for production code
- To be async by default without sacrificing readability
- To be small and get out of the way. The HTTP layer is not the most interesting part of your application and it should not be the most complex one either.

#### How to install it

```Groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation 'com.github.undertow-io:snitch:0.1.0'
}
```

#### Getting started

```Kotlin
UndertowSnitchService(GsonJsonParser)
    .setRoutes {
        GET("/hello") isHandledBy { "world".ok }
    }
    .startListening()
    .generateDocumentation()
    .servePublicDocumenation()
```

### Features
#### Expressive routing
```Kotlin
val usersController = routes {
    POST() with body<CreateUserRequest>() isHandledBy createUser
    POST("login") with body<LoginRequest>() isHandledBy userLogin

    userId / "posts" / {
        authenticated {
            GET() onlyIf principalEquals(userId) isHandledBy getPosts
            POST() onlyIf principalEquals(userId) with body<CreatePostRequest>() isHandledBy createPost

            GET(postId) isHandledBy getPost
            PUT(postId) with body<UpdatePostRequest>() onlyIf principalEquals(userId) isHandledBy updatePost
            DELETE(postId) onlyIf (principalEquals(userId) or hasAdminRole) isHandledBy deletePost
        }
    }
}
```

#### Expressive handlers
```Kotlin
...
val getPosts by handling {
    postsRepository().getPosts(principal).toResponse.ok
}

val createPost by parsing<CreatePostRequest>() handling {
    postsRepository().putPost(
        CreatePostAction(
            principal,
            PostTitle(body.title),
            PostContent(body.content),
        )
    )
    .mapSuccess {
        SuccessfulCreation(value).created
    }.mapFailure {
        FailedCreation().badRequest()
    }
}
...
```

#### Expressive and exendable parameter definition, parsing and validation
```Kotlin
val userId by path()
val postId by path()
val accessToken by header(validAccessToken)

// Your custom parameter validation and parsing logic
val validAccessToken = stringValidator("valid jwt") { jwt().validate(it) }
```
#### Middleware
Here's how you would define a simple logger middleware
```Kotlin
val Router.log get() = using {
        logger().info("Begin Request: ${request.method.name} ${request.path}")
        next().also {
            logger().info("End Request: ${request.method.name} ${request.path}")
        }
    }

val rootRouter = routes {
    log {
        "health" / healthController
        "users" / usersController
    }
}
```

#### Guards 
Endpoints can be configured with `onlyIf` and a condition or a condition expression to restrict access to the endpoint. 
```Kotlin
val usersController = routes {
    ...

    userId / "posts" / {
        authenticated {
            ...
            DELETE(postId) onlyIf (principalEquals(userId) or hasAdminRole) isHandledBy deletePost
        }
    }
}
```
Here `authenticated` is a guard that checks if the user is authenticated. `principalEquals` and `hasAdminRole` are defined as such in the user's codebase:
```Kotlin
val hasAdminRole = condition {
    when (role) {
        ADMIN -> Successful()
        else -> Failed(FORBIDDEN())
    }
}

fun principalEquals(param: Parameter<out Any, *>) = condition {
    if (principal.value == params(param.name)) Successful()
    else Failed(FORBIDDEN())
}
```

#### Why does it exist
Because code should be as readable as possible, and it should not require much ceremony to do simple stuff. If you want to do complex stuff it should be simple to extend the framework to do so. Resources are precious, you should not waste them on reflection or huge classpaths for doing basic stuff. Documentation is crucial, it should be generated automatically and be always up to date, and nobody should ever touch OpenAPI YAML or JSON files: we stand for these basic human rights.

#### Who is it for?
- It's for those who like their code to look good, fast and be well documented.
- Those who don't like pomp and ceremony to do their basic boring tasks
- For those who'd take a smaller tool over a larger one to do the same job
- For those who like to control-click on a method to see what it's doing and not end up in a reflection hell
- For those who like to see their code compile and not have to wait for the runtime to tell them they made a typo 

#### Who is it not for?
- Those who prefer a strictly object-oriented approach instead of function-first
   
#### System requirements
- Java 17 or higher
- Kotlin 1.8.20 or higher

#### How to contribute  
Fork the repository and submit a pull request. We will review it as soon as possible.
   
 