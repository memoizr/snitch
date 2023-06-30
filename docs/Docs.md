# snitch

#### will snitch on your api. with swagger.

### introduction

snitch is a small and typesafe web framework for kotlin

```kotlin
fun main() {
    snitch(gsonjsonparser).onroutes {
        get("hello") ishandledby { "world".ok }
    }.start()
} 
```

#### features

- lightweight and fast.
- functional approach
- openapi 3 support
- fully asynchronous execution
- plain kotlin. no reflection, code generation, annotation processing.
- kotlin compiler is enough. no gradle plugins

### getting started 

```kotlin
repositories {
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("com.github.memoizr:snitch-bootstrap:3.2.7")
}
```
that's it, no need for command line tools, gradle plugins. it's just a simple library.


### router
#### routing basics
```kotlin
val root = routes {
    get("foo") ishandledby {
        "bar".ok
    }
    post("foo") with body<foorequest>() ishandledby {
        "foovalue: ${body.foovalue}".created
    }
}
```        

the infix style is optional and a classic fluent approach is also supported.

```kotlin
val root = routes {
    get("foo").ishandledby {
        "bar".ok
    }
    post("foo")
        .with(body<foorequest>())
        .ishandledby {
        "foovalue: ${body.foovalue}".created
    }
}
```        

notice that `get("/foo")` and `get("foo")` are the same thing

you pass the router to the `onroutes` function 
```kotlin
fun main() {
    snitch(gsonjsonparser).onroutes(root).start()
} 
```

of course in a real application you'd like to separate the route declarations from the endpoint implementations.

```kotlin
val root = routes {
    get("foo") ishandledby getfoo
    post("foo") with body<foorequest>() ishandledby postfoo
}

val getfoo by handling {
    "bar".ok
}

val postfoo by parsing<foorequest>() handling {
    "foovalue: ${body.foovalue}".created
}
```

#### route nesting
services often have hundreds of routes, organized hierarchically. this can be modeled in snitch:
```kotlin
val root = routes {
    "health" / healthcontroller
    "users" / userscontroller
    "posts" / postscontroller
    ...
}

val userscontroller = routes {
    post() with body<createuserrequest> ishandledby createuser
    
    userid / {
        get() ishandledby getuser
        delete() ishandledby deleteuser
        
        "posts" / {
            get() ishandledby getposts
            post() with body<createpostrequest>() ishandledby createpost
            postid / {
                get() ishandledby getpost
            }
        }
    }
}
```

this will define the following routes:
```
post users
get users/{userid}
delete users/{userid}
get users/{userid}/posts
post users/{userid}/posts
get users/{userid}/posts/{postid}
```

different teams however will have different styles that they endorse, so for those who would rather have a less dry but more explicit route declaration, they can define the routes as:

```kotlin
val root = routes {
    healthcontroller
    userscontroller
    postscontroller
    ...
}
val userscontroller = routes {
    post("users") with body<createuserrequest> ishandledby createuser
    
    get("users" / userid) ishandledby getuser
    delete("users" / userid) ishandledby deleteuser

    get("users" / userid / "posts") ishandledby getposts
    post("users" / userid / "posts") with body<createpostrequest>() ishandledby createpost
    get("users" / userid / "posts" / postid) ishandledby getpost
}
```

the dsl is flexible so for teams that would like a measured and hybrid approach they can define the routes howerver they wish. for example grouping by path for all the actions supported on it:

```kotlin
val root = routes {
    healthcontroller
    userscontroller
    postscontroller
    ...
}
val userscontroller = routes {
    "users" / {
        post() with body<createuserrequest> ishandledby createuser
    }
    "users" / userid / {
        get() ishandledby getuser
        delete() ishandledby deleteuser
    }

    "users" / userid / "posts" / {
        get() ishandledby getposts
        post() with body<createpostrequest>() ishandledby createpost
    }

    "users" / userid / "posts" / postid / {
        get() ishandledby getpost
    }
}
```


#### http input parameters

```kotlin
val userid by path()
val showdetails by query(ofboolean)

val root = routes {
    get("users" / userid / "profile")
        .with(showdetails)
        .ishandledby {
            val user = userid(request[userid])
            if (request[showdetails]) {
                usersrepository().profiledetails(user)
            } else {
                usersrepository().profilesummary(user)
            }.ok
        }
}
```        

note: `userid` and `showdetails` are typed and validated. `request[showdetails]` will return a `boolean` and `request[userid]` will return a `string`. if you don't pass a `validator` such as `ofboolean`, it defaults to `ofnonemptystring`.  
note: you have to declare the usage of a certain parameter in order to use it.

#### input parameter validation and transformation
all parameters are validated and transformed to another type by default. here's some more examples, let's add the type parameters explicitly so it's clear what's happening:

```kotlin
val userid: string by path(nonemptystring)  
val filters: set<string> by path(ofnonemptyset)
val showdetails: boolean by path(ofboolean)
```

#### custom validations
although there are a few built in validator-transformers, they offer a relatively weak typing. best practice involves transforming and validating raw platform types into domain types. for example a `userid` is rarely actually just a string, for example it's unlikely the content of `shakespeare.txt` parsed as string could possibly be a valid id for a user. you most likely have a `value class userid` defined somewhere. likewise, a search filter is usually something like an `enum` where you have a set of pre-determined filter values. 

defining custom validator-transformers in snitch is simple:
```kotlin
value class userid(val id: uuid)
enum class filter { expired, active, cancelled, pending }

val ofuserid = validator<string, userid> { userid(uuid.fromstring(it)) }

// explicit types can be omitted for conciseness, here included for illustrative purposes
val userid: userid by path(ofuserid)
val filters: collection<filter> by query(ofrepeatableenum<filter>())
val filter: filter by query(ofenum<filter>())
```

> *note:* snitch is optimized for production code use cases, and in the spirit of kotlin, it *enforces* best practices. in production, you almost always need to validate and transfrom inputs consistently. snitch lets you do this in only one line of code in most cases, leading to a more concise, explicit and consistent codebase, making it easier to maintain larger codebases and for new developers to quickly become productive. 

#### optional input parameters
declaring a parameter with `query` or `header` will make it required. if the parameter is not supplied a `400` message will be returned specifying that that particular parameter was expected but not provided, as well as any other parameter that also does not pass validation. optional parameters can be declared as such:


```kotlin
// request[sort] is nullable
val sort: sorting? by optionalquery(ofenum<sorting>())
```

the optionality functionality is quite powerful, offering a clear and consistent way of specifying default values as well as defining a behaviour for when these values are provided as empty as or as invalid inputs:

```kotlin
// request[sort] is not nullable, new is the default value
val sort: sorting by optionalquery(ofenum<sorting>(), default = new)

val limit: int by optionalquery(ofnonnegativeint, default = 20, emptyasmissing = true, invalidasmissing = true)
val offset: int by optionalquery(ofnonnegativeint, default = 0, emptyasmissing = true, invalidasmissing = true)
```

#### parameter naming
snitch aims at being as concise and as less verbose as possible while delivering a full feature set for production use-cases. in this spirit when you define an input parameter such as `val q by query()` it will create a named query parameter that should be supplied as such for example:`?q=urlencodedquery`. note that the name of the parameter `val` in the codebase is by default the same name as in the api. if you want it to be different, it's simple:
```kotlin
val searchquery by query(name = "searchquery")
```

`limit` and `offset` here are defined so that if these parameters were not provided, or provided incorrectly, a default value would be provided instead. this is in case a "fail quietly" behaviour is desired. by default, a `fail explicitly` behaviour is supported, so empty or invalid inputs will return a 400 to inform the api user they're probably doing something wrong.

#### unsafe, undocumented parameter parsing
while snitch *enforces* best practices, leading to a less verbose and more consistent codebase that implements them, it also supports an *unsafe* traditional approach. if you want to access a parameter sneakily, and you don't care for the parameter to be included in the documentation, you can do it very simply with the cowboy-friendly syntax:

```kotlin
val getcows by handling {
    ...
    request.queryparams("numberofcows")
    request.headerparams("ranch")
    request.pathparams("ranchid")
    ...
}
```
although this approach is supported for niche use cases, it is strongly discouraged that this be used for most production applications unless there is a good reason for it.

#### repeated parameters
in http one of the hidden challenges to creating a robust and production grade api is that of handling the edge case of query or header parameters provided repeatedly when exactly one or at most one is expected. by default `val searchquery by query()` expects exactly one value being provided and `val searchquery by optionalquery()` provides at most one semantics, unexpected repetition will result in 400. support for repeated parameters can be made explicity by using `... by query(ofstringset)` for example, which uses a repeatable validator. custom validator for repeatable can be created in a very similar way to non-repeatable validators:

```kotlin
val ofuserid = repeatablevalidator<string, userid> { userid(uuid.fromstring(it)) }
```
#### body parameter
body parameters are treated differently than other input parameters as they are used in different ways. while it's common to share the same query parameters or headers between several endpoints (for example consider `limit`, `offset`, `orderby`, `access-token` and so on), body parameters are often single use. snitch aims at encouraging best  practices while reducing verbosity and clutter as much as possible, and in that spirit body parameter types are declared very simply:
```kotlin
post("mypath") with body<myrequest>() ishandleby {
    // already parsed to myrequest domain type
    request.body
}
```
this approach is typesafe, so if you were to omit the declaration of the body type, it would not be possible for you to access it within the handler:

```kotlin
post("mypath") ishandleby {
    // this resolves to kotlin's nothing special type and would not compile
    request.body
}
```
binary path can also be supported inituitively by: `with(body<bytearray>())`

### middleware 
snitch supports a very powerful and flexible middleware mechanism that can be used to implement a wide variety of features. let's see how you can use it to create a simple logging behaviour applied to a route hierarchy:

```kotlin
val router.log get() = decoratewith {
    logger().info("begin request: ${request.method.name} ${request.path}")
    next().also {
        logger().info("end request: ${request.method.name} ${request.path} ${it.statuscode.code} ${it.value(parser)}")
    }
}

val rootrouter = routes {
    log {
        "health" / healthcontroller
        "users" / userscontroller
    }
}
```

here `log` is a custom defined middleware logging behaviour. its usage is very intuitive, and it's clear that such behaviour should be applied to any route defined within its block. defining a new middleware is as straightforward as possible, here's the identity middleware, that simply calls the next action:
```kotlin
val router.identity get() = decoratewith { next() }
```

the code block provided to `decoratewith` works similarly to the way handlers work, you can still access the request parameter in the same way with `request[myparam]` and can return responses with `ok` `created` `badrequest()` etc like in normal handlers.

calling `next()` executes the code in the block of any nested middleware until it gets to the code block of the handler. `next()` returns the response from the next layer of the middleware and as such it can be transformed as appropriate. 

#### order of execution
the order of execution, that is, what code is executed by the `next()` call, is dependent on the order of declaration. it works as your intuition would expect, inside out, from most nested to least nested:
```kotlin
//called second
log {
    // called first
    statistics {
        get() ...
    }
}
```

### security and access control
middleware allows for the implementation of powerful and granular access control systems. here's a realistic example:

```kotlin
val router.authenticated
    get() = transformendpoints {
        with(listof(accesstoken)).decorate {
            when (request[accesstoken]) {
                is authentication.authenticated -> next()
                is authentication.unauthenticated -> unauthorized()
            }
        }
    }

val accesstoken: authentication by header(validaccesstoken)

val validaccesstoken = validator<string, authentication> { jwt().validate(it) }

sealed interface authentication {
    data class authenticated(val claims: jwtclaims) : authentication
    interface unauthenticated : authentication
    object invalidtoken : unauthenticated
    object expiredtoken : unauthenticated
    object missingtoken : unauthenticated
    object invalidclaims : unauthenticated
}
```

and this is how this is used
```kotlin

authenticated {
        ...
        get(userid / "posts") ishandledby getposts
        ...
    }
}
```
now, there's a lot to unpack in a few lines of code, let's break it down:
```kotlin
val router.authenticated
    get() = decorateendpoints {
        with(listof(accesstoken)).decorate {
            when (request[accesstoken]) {
                is authentication.authenticated -> next()
                is authentication.unauthenticated -> unauthorized()
            }
        }
    }
```

`decorateendpoints` will apply whatever transformation inside the block to any endpoint to which this will be applied. `with(listof(accesstoken))` is declaring and adding the `accesstoken` header parameter to the endpoints, documentation will reflect that. `request[accesstoken]` parses, validates and transforms the access token provided in the headers. it returns a domain type, and we can proceed to the next layer of middleware in case the token is valid, and return a 401 error in case it is not.

### database integration
snitch is an http focused tool, and as such it abstains from offering solutions to non-http problems such as deeply integrating with databases. we believe it is better to leave that job to specialized tools such as jooq or exposed. that said what snitch does offer is an extremely easy way of integrating with such tools. for example, here's how simple it is to declare that endpoints within a given hierarchy should all execute the code within a `exposed` transaction:

```kotlin
withtransaction {
    post() with body<createuserrequest>() ishandledby createuser
    post("login") with body<loginrequest>() ishandledby userlogin

    userid / "posts" / {
        authenticated {
            get() onlyif principalequals(userid) ishandledby getposts
            post() onlyif principalequals(userid) with body<createpostrequest>() ishandledby createpost

            get(postid) ishandledby getpost
            put(postid) with body<updatepostrequest>() onlyif principalequals(userid) ishandledby updatepost
            delete(postid) onlyif (principalequals(userid) or hasadminrole) ishandledby deletepost
        }
    }
}
```

here's how withtransaction is implemented:
```kotlin
// transaction {} from exposed framework
val router.withtransaction get() = decoratewith { transaction { next() } }
```
the ease with which this feature can be implemented is a testament to the power and flexibility of middleware. this can also be done in a granular way, by endpoint:
```kotlin
get() decorated withexposedtransaction onlyif principalequals(userid) ishandledby getposts
```
and this is the declaration of this decoration, which can be reused across different endpoints:
```kotlin
val withexposedtransaction = decoration { transaction { next() } }
```
this code hardly needs any explanation, in the spirit of snitch philosophy. 

the transaction example was just one way of showing how the flexibility and power of the dsl makes it extremely convenient to integrate with purpose built tools for database and other purposes. snitch focuses on http, but it seamlessly integrates with other tools with other focuses.

### guards
still on top of the same underlying mechanism we've built a powerful and granular guard mechanism, here's an example of it at work:

```kotlin
val requestwrapper.role: role get() = (request[accesstoken] as authentication.authenticated).claims.role

val hasadminrole = condition {
    when (role) {
        admin -> successful()
        else -> failed(forbidden())
    }
}
```
and this is how it's used
```kotlin
delete(postid) onlyif hasadminrole ishandledby deletepost
```

`onlif` takes a condition which can be either `successful` or `failed` and will either proceed with the request or terminate early accordingly.

this offers a high degree of granularity when specifying access control as applied to individual routes.

#### composing conditions
conditions are composable and support basic boolean logic operations:

```kotlin
delete(postid) onlyif (principalequals(userid) or hasadminrole) ishandledby deletepost
```

the code above hardly needs an explanation for what it's doing, despite the fact that it's not trivial behaviour.

here's the definition of `principalequals`:
```kotlin
fun principalequals(param: parameter<out any, *>) = condition {
    if (principal.value == request[param]) successful()
    else failed(forbidden())
}

val requestwrapper.principal: userid get() = (request[accesstoken] as authentication.authenticated).claims.userid
```

#### reusing conditions
although it's possible to customize each and every endpoint to lock it down to the exact security guarantees your business logic needs to enforce, it's often the case that you need to share the same guard logic across several endpoints. snitch offers two ways of doing this, the first one is obvious:

```kotlin
val owneroradmin = principalequals(userid) or hasadminrole

delete(postid) onlyif owneroradmin ishandledby deletepost
```

the second one is even more generic, as it can be applied to an entire sub-hierarchy of routes. it works similarly to how middleware does:

```kotlin
onlyif(principalequals(userid) or hasadminrole) {
    ...
    delete(postid) ishandledby deletepost
    patch(postid) with body<updatepostrequest>() ishandledby updatepost
    ...
}
```

note that this approach to guards is in line with what we call "snitch's way" or "snitchy". of course good old imperative checks inside the handler are still possible and supported, and in some cases that's the best thing to do. but sticking to snitch's way allows for more consistent, readable and manageable codebases at any scale.

### error handling
although snitch encourages a more functional approach to errors, it also supports global exception handling for both unexpected behaviour and for flow control.

```kotlin
snitch(gsonjsonparser)
    .onroutes(root)
    .handleexception(myexception::class) { exception ->
        mycustomerroresponse(exception.reason)
            .also { logger().error(it.tostring()) }
            .badrequest()
    }
    .start()
```

note that the body of the exception handler works like the normal handlers', with the only difference that it has a referfence to the exception being handled, thie `it` of the lambda, which can be optionally named as in the example above. note that it's not necessary to return an error response, it's possible to return an alternative successful response instead. you can see that there is a lot of functionality packed in a small amount of code, yet it still remains fairly intuitive and readable.

#### polymorphic error handling
note that error handling is polymorphic, so if `myexception` extends `mybaseexception` `.handleexception(mybaseexception::class)...` would handle `myexception` as well as any other subclass of `mybaseexception`. for this reason, ordering of the declaration of exception handlers matters. you should always put the most specific handlers first, otherwise a more generic polymorphic handler would handle the exception instead. note that this feature implementation relies on some reflection, and while it's relatively efficient, it's not as efficient as a more functional approach. for that reason this should not be used as a main flow control mechanism for performance critical applications.

### testing
snitch supports a fluent and concise integration testing dsl. in accordance with the rest of the library, it is designed with simplicity, intuitiveness and readability in mind. the expressiveness and simplicity is aimed at encouraging a tdd approach. here is what simple tests would look like, including an example of the base class and application object:
```kotlin
class healthtest : basetest() {
    @test
    fun `foo does bar`() {
        get("/foo/bar")
            .expectcode(200)
            .expectbody("""{"status": "ok"}""")
    }
    
    @test
    fun `post`() {
        post("/foo/bar")
            .withbody(myrequestbody)
            .expectcode(200)
            .expectbody("""{"status": "ok"}""")
    }
}

@testinstance(testinstance.lifecycle.per_class)
abstract class basetest : snitchtest({ application.setup(it) }) {
    @beforeall
    fun beforeall() = super.before()

    @afterall
    fun afterall() = super.after()
}

object application {
    fun setup(port: int): routedservice =
        snitch(gsonjsonparser, snitchconfig(service(port = port)))
            .onroutes(rootrouter)
            .handleexceptions()
}
```
please refer to the `example` module in the repository source code for more in-depth examples involving real-world use cases complete with database setup and access, dependency injection and more.

### intellij integration
snitch has the best in class intellij integration plugin and it ships by default with the ide: jetbrain's kotlin plugin. that's all that's needed to unleash the full power of snitch and have autocompletion, syntax higlighting and so on. because snitch aims at being plain kotlin only, without reflection or annotation processing or code generation, the kotlin compiler is fully capable of understanding each aspect of the library and its uses. snitch usage errors are more often than not resolved at compile time. one of the leading design principles of snitch is that the user of the library should always be able to use the ide to navigate to middleware definitions, follow the nesting of routes upsream and downstream, and so on. a user should never be in the position of not knowing what some code does. they might not necessarily understand every aspect of how the internals work, but they should at the very least be able to see the internals, and explore them with their ide. the pure kotlin approach makes this easy.


### Showcase

#### DSL extension usecase: API versioning

It is often the case that your API will need versioning for backwards compatibility with deployed clients. There are several approaches to versioning. A common and annoying problem is handling relatively minor version differences on only a subset of endpoints. Setting up a whole separate path hierarchy may be an overkill in such cases. E.g you want to have these routes:

```
GET /v1/hey/there/foo
GET /v2/hey/there/foo
GET /v1/hey/all/bar
```

This is how easy it is to extend Snitch's DSL to support this type of versioning:

```kotlin
val baseVersion = "v1"

baseVersion / {
    "hey" / {
        "there" / {
            GET("foo") isHandledBy { "this is foo v1".ok }
            GET("foo").v2 isHandledBy { "this is foo v2".ok }
        }
        "all" / {
            GET("bar") isHandledBy { "this is bar v1".ok }
        }
    }
}

// demo only, use a more robust path editing logic for production
val <T : Any> Endpoint<T>.v2 get() = copy(path = path.replace("/$baseVersion/", "/v2/"))
```

And that's it. Endpoints are data classes and can be customized like any other data class in Kotlin.  

This style is also supported, if you prefer:
```kotlin
GET("foo") v 2 isHandledBy { "this is foo v2".ok }
```
This uses an extension function instead of an extension property.
```
// demo only, use a more robust path editing logic for production
infix fun <T: Any> Endpoint<T>.v(version: Int) = copy(
    path = path.replace("/$baseVersion/", "/v$version/")
)
```