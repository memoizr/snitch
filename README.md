![Workflow ci](https://github.com/memoizr/snitch/actions/workflows/ci.yml/badge.svg)
[![](https://jitpack.io/v/memoizr/snitch.svg)](https://jitpack.io/#memoizr/snitch)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Code Coverage](https://img.shields.io/badge/Code%20Coverage-83%25-brightgreen)]()

## Snitch 
Snitch helps you create a production-grade HTTP layer for your applications and (micro)services with minimal effort. To create ***complete*** documentation for them with ***no*** effort.

Snitch is a thin layer built upon long established and well-supported embedded web servers such as Undertow. Thanks to Kotlin's inlining capabilities the performance of Snitch is essentially the same as that of the underlying web server.

Our primary goals are:
- To create the most readable and maintainable API for creating web services
- By readable we mean readable production code where routes are complex, parameters need validation, parsing and mapping to domain types, errors need to be handled, deal with authentication and permissions, etc.
- To fully automate the creation of documentation for your services 
- To have a strongly typesafe approach to HTTP layer modelling
- To be as lightweight and performant as possible
- To not use any reflection, annotations or code generation for production code
- To be async by default without sacrificing readability
- To be small and get out of the way. The HTTP layer is not the most interesting part of your application and it should not be the most complex one either.
- To have a small learning curve despite it being a DSL

#### How to install it

```Groovy
repositories {
    maven { url "https://jitpack.io" }
}

dependencies {
    implementation 'com.github.memoizr.snitch:bootstrap:3.2.3'
}
```

#### Getting started

```Kotlin
snitch(GsonJsonParser)
    .onRoutes {
        GET("/hello") isHandledBy { "world".ok }
    }
    .start()
    .serveDocumenation()
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
val postId by path(ofPostIdValidator)
val accessToken by header(ofValidAccessToken)

// Your custom parameter validation and parsing logic
val ofValidAccessToken = stringValidator("valid jwt") { jwt().validate(it) }
```

#### Strongly typed inputs and outputs
Headers, paths, query parameters and bodies always require validation and mapping to domain types. Snitch makes it easy to do so.
```Kotlin 
val limit by query(ofNonNegativeInt(max = 30))
val offset by query(ofNonNegativeInt())

val postId by path(ofPostIdValidator)
```
In the example above `limit` and `offset` are validated and mapped to `UInt` values. `postId` is validated and mapped to a `PostId` value object.

Therefore in the handler you can do this:
```Kotlin
val getPosts by handling {
    postsRepository().getPosts(request[userId], request[limit], request[offset]).toResponse.ok }
```
The parameters are validated and safe to use. If they are not valid the request will not be handled and the client will receive a `400 Bad Request` response with a list of reasons about all the parameters which are either not supplied or invalid.

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

#### Fully automated documentation
Snitch outputs OpenAPI 3.0 docs. Absolutely any of the inputs and any of the outputs are documented automatically, even response codes. No work or setup is required from the user to achieve this. Snitch allows the user to specify whether to serve the documentation on a given route. This is an interactive page based on Swagger-UI that allows a viewer to interact with the API, authenticate, and make any request and see the responses. You can optionally annotate individual fields of requests, responses or parameters to add additional descriptions or provide example values so that it'd be straightforward to use the API through the documentation page, as requests would be pre-populated with such example values. Documentation can either be served on the same service, or given to a doc aggregator that allows to provide a unified API for multiple services. Request, Response and parameter types are automatically generated exclusively from type information from the Kotlin language, hence they always represent the actual behaviour of the API.


Adding this line to the service configuration will generate the documentation and serve it on the `/docs` route, which can be configured along with the rest of the application settings.
```Kotlin
    .generateDocumentation()
    .servePublicDocumenation()
```
This is an example screenshot of the interactive Swagger-UI page that it serves at the `/docs` route:   

![Swagger-UI](https://i.imgur.com/6Z2Z3ZM.png)

Every endpoint can be interacted with, and the documentation is always up to date with the actual behaviour of the API.

#### Performance
Snitch uses Undertow as the default embedded web server. Undertow was chosen because it's the fastest embedded web server for the JVM for most use cases, and for its async-first approach based on NIO. It's also the most lightweight one, with a small classpath and a small memory footprint. That said, it's possible to use any other web server with Snitch, as it's not tied to Undertow in any way, so if another web server is preferred it can be used instead.

Snitch is designed to be only a very thin overlay on top of the embedded webserver, so it does not add any overhead to the request handling process. It's designed to be as fast as possible, and it's therefore almost as fast as the underlying web server is. Object instantiation is kept to a minium to not cause GC pressure. Call stack is also kept as shallow as possible.
Reflection is not used at all, and the only reflection that happens is when the documentation is generated, which is an optional step for production code that would occour at most once during startup. This is done only once, and the results are cached, so it does not happen on every request. 
The application has a small classpath, and the only dependencies are the web server and the JSON library. This means that the application can be deployed as a single jar file, and it's easy to deploy and run it in a containerized environment. A small application complete with documentation can run in as little as 12MB of ram on top of the JVM. This library has been successfuly used in a variety of settings, from resource constrained environments such as Android to high performance environments such as high throughput microservices. 


#### Small learning curve
The library is actually very small and a complete detailed overview can be had in a matter of hours. Powerful features such as guards, middleware and others are built on top of a small number of core concepts, and really there isn't any magic involved. The codebase only has a few thousand lines of code. Given a fluency of the Kotlin language it should be one of the easiest frameworks to truly understand how it works. Really it's just a thin wrapper around the embedded web server.

#### Learning resources
Please refer to the 'example` folder for a reference implementation of a simple reddit-like service. The example tries to have production-like qualities such as handling authorization and authentication, database interaction, it shows how it works well and integrates with dependency injection libraries such as Shank. Also please refer to the wiki for a more detailed overview of the library as well as tutorials.

#### Community support
Join us on our slack channel at https://join.slack.com/t/snitch-f to ask questions, get help, or just to chat. We're a small but growing community but we're friendly and we're always happy to help.

#### Why does it exist
Because code should be as readable as possible, and it should not require much ceremony to do simple stuff. If you want to do complex stuff it should be simple to extend the framework to do so. Resources are precious, you should not waste them on reflection or huge classpaths for doing basic stuff. Documentation is crucial, it should be generated automatically and be always up to date, and nobody should ever touch OpenAPI YAML or JSON files: we stand for these basic human rights.

#### Who is it for?
- It's for those who like their code to look good, fast and be well documented.
- Those who don't like pomp and ceremony to do their basic boring tasks
- For those who'd take a smaller tool over a larger one to do the same job
- For those who like to control-click on a method to see what it's doing and not end up in a reflection hell
- For those who like to see their code compile and not have to wait for the runtime to tell them they made a typo 
- Those who like aspects of Object Oriented as well as Functional Programming and want to use both to their advantage
   
#### System requirements
- Java 17 or higher
- Kotlin 1.8.20 or higher

#### How to contribute  
This library evolved over the years, initially as an internat tool deployed in a variety of scenarios, but now released as an open-source project. To contribute, please fork the repository and submit a pull request. We will review it as soon as possible. Alternatively, open an issue and we'll try to address it as soon as possible.
   
 
