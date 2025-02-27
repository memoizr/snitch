---
slug: the-inspiration-behind-snitch
title: The Inspiration Behind Snitch - Borrowing from the Best
authors: [snitch-team]
tags: [snitch, web-development, kotlin, inspiration, frameworks]
---

# The Inspiration Behind Snitch: Borrowing from the Best

Creating a new web framework isn't a decision to take lightly. The ecosystem is already crowded with options across many languages, each with their strengths and devoted communities. So when we built Snitch, we didn't start from scratch - instead, we carefully studied what worked well in other frameworks and what didn't, then synthesized those lessons into something new that addresses the specific needs of Kotlin developers.

<!-- truncate -->

In this post, I'll walk through the frameworks and libraries that inspired key aspects of Snitch, and how we adapted those ideas into a cohesive whole that leverages Kotlin's strengths.

## Expressive Routing: Learning from Sinatra and Express

Snitch's routing DSL draws significant inspiration from Ruby's Sinatra and JavaScript's Express frameworks, both known for their clean, readable route definitions:

**Sinatra (Ruby):**
```ruby
get '/hello/:name' do
  "Hello #{params[:name]}!"
end
```

**Express (JavaScript):**
```javascript
app.get('/hello/:name', (req, res) => {
  res.send(`Hello ${req.params.name}!`);
});
```

**Snitch (Kotlin):**
```kotlin
GET("hello" / name) isHandledBy {
  "Hello ${request[name]}!".ok
}
```

While these approaches appear similar, Snitch adds several meaningful improvements:

1. **Type safety**: Sinatra and Express both use dynamic string parameters, while Snitch parameters are explicitly defined with types
2. **Hierarchical routing**: Snitch's route nesting approach makes complex API hierarchies much more readable
3. **Infix notation**: The `isHandledBy` infix function makes the route-to-handler relationship exceptionally clear

The nested routing approach was also influenced by the excellent Ktor framework, but with a focus on more explicit parameter handling.

## Middleware Inspiration: Express and Ktor

The middleware pattern in Snitch draws inspiration from both Express and Ktor, but with a Kotlin-specific twist.

**Express (JavaScript):**
```javascript
const logMiddleware = (req, res, next) => {
  console.log(`${req.method} ${req.path}`);
  next();
  console.log('Request completed');
};

app.use(logMiddleware);
```

**Ktor (Kotlin):**
```kotlin
install(CallLogging) {
  level = Level.INFO
}
```

**Snitch (Kotlin):**
```kotlin
val Router.log get() = decorateWith {
  logger.info("Begin: ${request.method} ${request.path}")
  next().also {
    logger.info("End: ${request.method} ${request.path}")
  }
}

routes {
  log {
    // Routes here
  }
}
```

While Express's approach is very flexible, it relies on side effects and mutation. Ktor's approach is more declarative but sometimes less flexible. Snitch strikes a balance:

1. **Composition over configuration**: Like Express, middleware can be composed easily
2. **Extension properties**: Using Kotlin's extension properties creates an intuitive DSL
3. **Functional approach**: The `next()` function creates a clean flow control similar to Express but with proper lexical scoping
4. **Immutable approaches**: No shared mutable state between middlewares

## Before/After Actions: Rails and AspectJ

The before/after action system in Snitch was inspired by Ruby on Rails' filters and AspectJ's aspect-oriented programming concepts.

**Rails (Ruby):**
```ruby
class UsersController < ApplicationController
  before_action :authenticate_user
  after_action :log_activity
  
  def show
    @user = User.find(params[:id])
  end
end
```

**Snitch (Kotlin):**
```kotlin
GET("users" / userId)
  .doBefore { authenticate() }
  .doAfter { logActivity() }
  .isHandledBy {
    userRepository.findUser(request[userId]).ok
  }
```

The key improvements in Snitch's implementation:

1. **Route-specific actions**: Actions can be applied at the individual route level, not just controller-wide
2. **Method chaining**: Clean, fluent API compared to Rails' class-level declarations
3. **Type safety**: Full access to request parameters in a type-safe manner
4. **Explicit ordering**: The execution order is clearly visible in the code

## Validator System: Inspired by Play Framework and Joi

Snitch's validator system combines ideas from Play Framework's form binding and JavaScript's Joi validation library.

**Play Framework (Scala):**
```scala
case class User(name: String, age: Int)

val userForm = Form(
  mapping(
    "name" -> nonEmptyText,
    "age" -> number(min = 0, max = 120)
  )(User.apply)(User.unapply)
)
```

**Joi (JavaScript):**
```javascript
const schema = Joi.object({
  name: Joi.string().required(),
  age: Joi.number().min(0).max(120).required()
});
```

**Snitch (Kotlin):**
```kotlin
data class User(val name: String, val age: Int)

val ofUser = validator<Map<String, String>, User> { map ->
  val name = map["name"] ?: throw ValidationException("Name required")
  val ageStr = map["age"] ?: throw ValidationException("Age required")
  val age = ageStr.toIntOrNull() ?: throw ValidationException("Age must be a number")
  
  if (age < 0 || age > 120) throw ValidationException("Age must be between 0 and 120")
  
  User(name, age)
}
```

Snitch's validator system offers several advantages:

1. **Transformation, not just validation**: Validators both validate and transform data into domain types
2. **Kotlin-native**: Leverages Kotlin's type system rather than external DSLs
3. **Composable validators**: Validators can be combined and reused
4. **Automatic documentation**: Validators automatically feed into API documentation

## Conditions System: Spring Security and Rails CanCan

The conditions system in Snitch draws inspiration from Spring Security's expression-based access control and Ruby's CanCan authorization library.

**Spring Security (Java):**
```java
@PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.id")
public User getUser(@PathVariable Long userId) {
  return userRepository.findById(userId);
}
```

**CanCan (Ruby):**
```ruby
class Ability
  include CanCan::Ability
  
  def initialize(user)
    can :manage, Post, user_id: user.id
    can :manage, :all if user.admin?
  end
end
```

**Snitch (Kotlin):**
```kotlin
val hasAdminRole = condition("hasAdminRole") {
  when (role) {
    ADMIN -> ConditionResult.Successful
    else -> ConditionResult.Failed("Not an admin".forbidden())
  }
}

val isResourceOwner = condition("isResourceOwner") {
  if (principal.id == request[resourceId]) ConditionResult.Successful
  else ConditionResult.Failed("Not the resource owner".forbidden())
}

GET("resource" / resourceId) onlyIf (isResourceOwner or hasAdminRole) isHandledBy { getResource() }
```

Snitch's condition system provides several key improvements:

1. **Composable boolean logic**: Operators like `and`, `or`, and `not` work naturally
2. **Route-level application**: Conditions can be applied at the route level for fine-grained control
3. **Custom error responses**: Each condition can specify its own error response
4. **Type safety**: Leverages Kotlin's type system for compile-time validation
5. **Self-documenting**: Conditions automatically document access requirements in API docs

## Automated Documentation: Swagger and Springfox

Snitch's automatic documentation system was inspired by Swagger/OpenAPI and tools like Springfox, but with a focus on zero-config operation.

**Springfox (Java):**
```java
@ApiOperation(value = "Get a user by ID", notes = "Requires authentication")
@ApiResponses(value = {
    @ApiResponse(code = 200, message = "Success", response = User.class),
    @ApiResponse(code = 404, message = "User not found")
})
@GetMapping("/users/{id}")
public ResponseEntity<User> getUser(@PathVariable("id") @ApiParam(value = "User ID") Long id) {
    // ...
}
```

**Snitch (Kotlin):**
```kotlin
// No annotations required - documentation is derived from code
val userId by path(ofNonNegativeInt)

GET("users" / userId) isHandledBy {
    val user = userRepository.findUser(request[userId])
    if (user != null) user.ok
    else "User not found".notFound()
}
```

Snitch's documentation approach offers substantial benefits:

1. **Zero annotations**: No extra code needed for documentation
2. **Always accurate**: Documentation derived directly from routes, parameters, and handler code
3. **Type information**: Response types automatically included in documentation
4. **Interactive UI**: Swagger UI available automatically
5. **No out-of-sync docs**: Documentation cannot become outdated as it's generated from code

## Performance Inspiration: Undertow and Vert.x

Snitch's performance focus was inspired by high-performance servers like Undertow and Vert.x.

**Undertow (Java):**
```java
Undertow server = Undertow.builder()
    .addHttpListener(8080, "localhost")
    .setHandler(exchange -> {
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
        exchange.getResponseSender().send("Hello World");
    }).build();
server.start();
```

**Vert.x (Java):**
```java
Vertx vertx = Vertx.vertx();
HttpServer server = vertx.createHttpServer();

server.requestHandler(request -> {
  request.response()
    .putHeader("content-type", "text/plain")
    .end("Hello world!");
});

server.listen(8080);
```

**Snitch (Kotlin):**
```kotlin
snitch(GsonJsonParser)
    .onRoutes {
        GET("hello") isHandledBy { "Hello world".ok }
    }
    .start()
```

While Snitch provides a higher-level API, it maintains excellent performance by:

1. **Thin abstraction layer**: Minimal overhead on top of Undertow
2. **No reflection in hot paths**: Avoiding the performance costs of reflection
3. **Inlining**: Using Kotlin's inline functions to eliminate function call overhead
4. **Minimal object creation**: Avoiding excessive object instantiation that could increase GC pressure

## DSL Design: Inspired by Kotlin's Built-in DSLs

Snitch's DSL design draws heavily on Kotlin's own built-in DSLs like the ones for building HTML or type-safe SQL with Exposed.

**Kotlin HTML DSL:**
```kotlin
html {
    head {
        title("HTML Example")
    }
    body {
        h1 { +"Hello, world!" }
        div {
            p { +"This is a paragraph" }
        }
    }
}
```

**Snitch (Kotlin):**
```kotlin
routes {
    "api" / {
        "v1" / {
            "users" / {
                GET() isHandledBy { getAllUsers() }
                POST() with body<CreateUserRequest>() isHandledBy { createUser() }
                
                userId / {
                    GET() isHandledBy { getUser() }
                    PUT() with body<UpdateUserRequest>() isHandledBy { updateUser() }
                }
            }
        }
    }
}
```

Snitch's DSL leverages Kotlin-specific features:

1. **Type-safe builders**: Like Kotlin's HTML DSL, but for HTTP routes
2. **Infix functions**: Creating a readable, almost English-like syntax
3. **Extension functions**: Extending existing types for a more natural API
4. **Lambdas with receivers**: Creating natural scoping and reducing boilerplate
5. **Immutable data structures**: Promoting safe, predictable code

## Conclusion: Best Ideas, Better Implementation

Snitch didn't try to reinvent the wheel. Instead, we carefully studied what made other frameworks great, then adapted those ideas to create something that takes full advantage of Kotlin's strengths while avoiding common framework pitfalls.

What makes Snitch special isn't just the features it borrowed, but how it integrated them into a cohesive whole that emphasizes:

- **Type safety** without verbosity
- **Explicitness** without boilerplate
- **Performance** without complexity
- **Flexibility** without magic

By standing on the shoulders of giants - taking inspiration from frameworks like Sinatra, Express, Rails, Spring, and Ktor - we've created something that feels natural and powerful for Kotlin developers.

We'd love to hear which aspects of Snitch you find most valuable, and what other frameworks you've used that have features you'd like to see adapted to the Kotlin ecosystem. Join us on [Discord](https://discord.gg/bG6NW3UyxS) to share your thoughts and experiences!