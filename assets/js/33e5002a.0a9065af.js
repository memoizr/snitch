"use strict";(self.webpackChunkguides=self.webpackChunkguides||[]).push([[6156],{369:(e,n,t)=>{t.r(n),t.d(n,{assets:()=>l,contentTitle:()=>r,default:()=>h,frontMatter:()=>o,metadata:()=>i,toc:()=>c});var i=t(8299),s=t(4848),a=t(8453);const o={slug:"unlocking-advanced-web-apis-with-snitch",title:"Unlocking Advanced Web APIs with Snitch",authors:["snitch-team"],tags:["snitch","web-development","kotlin","apis","dsl"]},r="Unlocking Advanced Web APIs with Snitch",l={authorsImageUrls:[void 0]},c=[{value:"The Essence of Snitch",id:"the-essence-of-snitch",level:2},{value:"The Validation System: Transform Raw Inputs into Domain Types",id:"the-validation-system-transform-raw-inputs-into-domain-types",level:2},{value:"Before and After Actions: Tackle Cross-Cutting Concerns Elegantly",id:"before-and-after-actions-tackle-cross-cutting-concerns-elegantly",level:2},{value:"Conditions: Sophisticated Access Control Made Simple",id:"conditions-sophisticated-access-control-made-simple",level:2},{value:"Decoration: Reusable Middleware Patterns",id:"decoration-reusable-middleware-patterns",level:2},{value:"Everything Unified by Documentation",id:"everything-unified-by-documentation",level:2},{value:"Performance Without Compromise",id:"performance-without-compromise",level:2},{value:"Conclusion: A Framework That Grows With You",id:"conclusion-a-framework-that-grows-with-you",level:2}];function d(e){const n={a:"a",code:"code",h2:"h2",li:"li",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,a.R)(),...e.components};return(0,s.jsxs)(s.Fragment,{children:[(0,s.jsxs)(n.p,{children:["Building production-grade HTTP APIs can be complex and time-consuming. Many frameworks offer simplicity at the cost of readability or performance when systems grow beyond simple examples. Today, I'm excited to introduce you to ",(0,s.jsx)(n.strong,{children:"Snitch"}),": a Kotlin HTTP framework that prioritizes readability and maintainability while delivering exceptional performance and a powerful feature set."]}),"\n",(0,s.jsx)(n.h2,{id:"the-essence-of-snitch",children:"The Essence of Snitch"}),"\n",(0,s.jsx)(n.p,{children:"Snitch was created to solve a fundamental problem: as web APIs grow more complex, codebase readability often suffers. Many frameworks prioritize conventions over explicit configuration, which can lead to obscure runtime errors and steep learning curves for new team members."}),"\n",(0,s.jsx)(n.p,{children:"Here's what makes Snitch different:"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"Expressive, readable DSL"})," that makes complex route hierarchies understandable at a glance"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"Strong type safety"})," that catches errors at compile time rather than runtime"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"Minimal overhead"})," by leveraging Kotlin's inline functions and a thin layer over high-performance servers"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"Automated documentation"})," generation without additional configuration"]}),"\n",(0,s.jsxs)(n.li,{children:[(0,s.jsx)(n.strong,{children:"No reflection magic"})," in production code, making it easier to understand and debug"]}),"\n"]}),"\n",(0,s.jsx)(n.p,{children:'For many developers, that "aha!" moment with Snitch comes when they first see how explicitly yet concisely they can model intricate API structures:'}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'val usersController = routes {\n    POST() with body<CreateUserRequest>() isHandledBy createUser\n    \n    userId / "posts" / {\n        authenticated {\n            GET() onlyIf principalEquals(userId) isHandledBy getPosts\n            POST() onlyIf principalEquals(userId) with body<CreatePostRequest>() isHandledBy createPost\n            \n            GET(postId) isHandledBy getPost\n            DELETE(postId) onlyIf (principalEquals(userId) or hasAdminRole) isHandledBy deletePost\n        }\n    }\n}\n'})}),"\n",(0,s.jsx)(n.p,{children:"This declarative style strikes an impressive balance between readability and expressiveness, making it immediately clear what routes are available and what security constraints apply."}),"\n",(0,s.jsx)(n.h2,{id:"the-validation-system-transform-raw-inputs-into-domain-types",children:"The Validation System: Transform Raw Inputs into Domain Types"}),"\n",(0,s.jsx)(n.p,{children:"Snitch's validator system addresses a critical challenge in HTTP API development: safely converting raw string inputs into strongly-typed domain objects. Let's look at what makes this system powerful:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'// Define custom domain types\ndata class UserId(val value: String)\ndata class PostId(val value: String)\n\n// Create validators that validate and transform raw inputs\nval ofUserId = validator<String, UserId>("valid user ID", """^[a-zA-Z0-9]{8,12}$""".toRegex()) {\n    UserId(it)\n}\n\n// Use validators with parameters\nval userId by path(ofUserId)\nval postId by path(ofPostId)\n\n// Access validated, typed parameters in handlers\nval getPost by handling {\n    val user: UserId = request[userId]\n    val post: PostId = request[postId]\n    \n    postRepository.findPost(user, post).ok\n}\n'})}),"\n",(0,s.jsx)(n.p,{children:"With this approach, validations happen before your handler code runs, so you're always working with properly validated, domain-specific types. If validation fails, the framework returns appropriate 400-level responses automatically, with descriptive error messages that help API consumers fix their requests."}),"\n",(0,s.jsx)(n.h2,{id:"before-and-after-actions-tackle-cross-cutting-concerns-elegantly",children:"Before and After Actions: Tackle Cross-Cutting Concerns Elegantly"}),"\n",(0,s.jsxs)(n.p,{children:["One of Snitch's newer features that I find particularly elegant is its ",(0,s.jsx)(n.strong,{children:"before and after"})," action system. This lets you execute code around your handlers in a clean, composable way:"]}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'GET("users" / userId)\n    .doBefore { \n        // Authentication check\n        if (request[accessToken] is Authentication.Unauthenticated) {\n            return@doBefore "Unauthorized".unauthorized()\n        }\n    }\n    .doBefore {\n        // Logging\n        logger.info("User access: ${request[userId]}")\n        request.attributes["startTime"] = System.currentTimeMillis()\n    }\n    .doAfter { \n        // Performance tracking\n        val duration = System.currentTimeMillis() - (request.attributes["startTime"] as Long)\n        metrics.recordRequestTime(request.path, duration)\n    }\n    .isHandledBy {\n        userRepository.findUser(request[userId]).ok\n    }\n'})}),"\n",(0,s.jsx)(n.p,{children:"What's fascinating about this approach is its flexibility and readability. The before/after actions have full access to the request context, can short-circuit execution with early responses, and can be composed together in intuitive ways. For cross-cutting concerns like logging, metrics, authentication, and authorization, this provides a clean separation of those aspects from your core business logic."}),"\n",(0,s.jsx)(n.h2,{id:"conditions-sophisticated-access-control-made-simple",children:"Conditions: Sophisticated Access Control Made Simple"}),"\n",(0,s.jsx)(n.p,{children:"Snitch's condition system is another gem worth exploring. It allows for incredibly expressive access control rules that are both readable and maintainable:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'val isResourceOwner = condition("isResourceOwner") {\n    if (principal.id == request[resourceId]) ConditionResult.Successful\n    else ConditionResult.Failed("Not the resource owner".forbidden())\n}\n\nval hasAdminRole = condition("hasAdminRole") {\n    when (role) {\n        ADMIN -> ConditionResult.Successful\n        else -> ConditionResult.Failed("Not an admin".forbidden())\n    }\n}\n\n// Apply conditions to endpoints\nDELETE("posts" / postId) onlyIf (isResourceOwner or hasAdminRole) isHandledBy deletePost\n'})}),"\n",(0,s.jsxs)(n.p,{children:["The most impressive aspect here is the support for logical operators (",(0,s.jsx)(n.code,{children:"and"}),", ",(0,s.jsx)(n.code,{children:"or"}),", ",(0,s.jsx)(n.code,{children:"not"}),") that work exactly as you'd expect, making complex access control rules both expressive and maintainable."]}),"\n",(0,s.jsx)(n.h2,{id:"decoration-reusable-middleware-patterns",children:"Decoration: Reusable Middleware Patterns"}),"\n",(0,s.jsx)(n.p,{children:"The decoration system in Snitch provides a flexible way to wrap behavior around route handlers:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:'val withLogging = decorateWith {\n    logger.info("Begin Request: ${request.method} ${request.path}")\n    next().also {\n        logger.info("End Request: ${request.method} ${request.path} ${it.statusCode} (${System.currentTimeMillis() - startTime}ms)")\n    }\n}\n\nval withTransaction = decorateWith {\n    transaction {\n        next()\n    }\n}\n\n// Apply to routes\nwithLogging {\n    withTransaction {\n        POST("orders") isHandledBy createOrder\n    }\n}\n'})}),"\n",(0,s.jsxs)(n.p,{children:["The ",(0,s.jsx)(n.code,{children:"next()"})," function is the key - it executes the next decoration in the chain or the handler itself, allowing for pre and post-processing while maintaining a clean control flow."]}),"\n",(0,s.jsx)(n.h2,{id:"everything-unified-by-documentation",children:"Everything Unified by Documentation"}),"\n",(0,s.jsx)(n.p,{children:"Perhaps the most impressive feature - one that developers typically dread implementing - is automatic API documentation. Snitch generates OpenAPI 3.0 documentation without any additional configuration:"}),"\n",(0,s.jsx)(n.pre,{children:(0,s.jsx)(n.code,{className:"language-kotlin",children:"snitch(GsonJsonParser)\n    .onRoutes(myRoutes)\n    .generateDocumentation()\n    .servePublicDocumentation()\n    .start()\n"})}),"\n",(0,s.jsx)(n.p,{children:"With this minimal setup, you get a full Swagger UI that accurately reflects your API, including:"}),"\n",(0,s.jsxs)(n.ul,{children:["\n",(0,s.jsx)(n.li,{children:"All routes and their HTTP methods"}),"\n",(0,s.jsx)(n.li,{children:"Path, query, and header parameters with their validators and descriptions"}),"\n",(0,s.jsx)(n.li,{children:"Request and response body schemas"}),"\n",(0,s.jsx)(n.li,{children:"Authentication requirements"}),"\n",(0,s.jsx)(n.li,{children:"Possible response status codes"}),"\n"]}),"\n",(0,s.jsx)(n.p,{children:"The magic here is that this documentation is derived directly from your code, so it's always accurate and up-to-date."}),"\n",(0,s.jsx)(n.h2,{id:"performance-without-compromise",children:"Performance Without Compromise"}),"\n",(0,s.jsx)(n.p,{children:"Beyond the elegant API, Snitch delivers impressive performance. By using Undertow as the default embedded server and carefully avoiding reflection and excessive object creation, it achieves near-native performance while maintaining its expressive DSL."}),"\n",(0,s.jsx)(n.p,{children:"A typical Snitch application has a tiny memory footprint (as low as 12MB of RAM on top of the JVM) and minimal startup time, making it suitable for everything from microservices to Android applications."}),"\n",(0,s.jsx)(n.h2,{id:"conclusion-a-framework-that-grows-with-you",children:"Conclusion: A Framework That Grows With You"}),"\n",(0,s.jsx)(n.p,{children:"What I find most compelling about Snitch is how it scales with complexity. Simple APIs remain simple, but as your requirements grow more sophisticated - with nested routes, complex access control, detailed validation, and cross-cutting concerns - the code remains readable and maintainable."}),"\n",(0,s.jsx)(n.p,{children:'Snitch achieves this by providing powerful abstractions that are composable and explicit, avoiding the "magic" that often makes frameworks hard to reason about as applications grow.'}),"\n",(0,s.jsx)(n.p,{children:"If you're building HTTP APIs in Kotlin and value both expressiveness and type safety, Snitch deserves a serious look. Its combination of a readable DSL, sophisticated features like validators and conditions, and exceptional performance makes it a compelling choice for professional API development."}),"\n",(0,s.jsxs)(n.p,{children:["To get started with Snitch, check out the ",(0,s.jsx)(n.a,{href:"/docs/tutorials/Mastering-Snitch-Parameters",children:"comprehensive tutorials"})," in our documentation section, or dive right into the ",(0,s.jsx)(n.a,{href:"https://github.com/memoizr/snitch/tree/master/example",children:"example application"})," on GitHub."]})]})}function h(e={}){const{wrapper:n}={...(0,a.R)(),...e.components};return n?(0,s.jsx)(n,{...e,children:(0,s.jsx)(d,{...e})}):d(e)}},8299:e=>{e.exports=JSON.parse('{"permalink":"/snitch/blog/unlocking-advanced-web-apis-with-snitch","editUrl":"https://github.com/memoizr/snitch/tree/main/guides/blog/2022-02-18-unlocking-advanced-web-apis-with-snitch.md","source":"@site/blog/2022-02-18-unlocking-advanced-web-apis-with-snitch.md","title":"Unlocking Advanced Web APIs with Snitch","description":"Building production-grade HTTP APIs can be complex and time-consuming. Many frameworks offer simplicity at the cost of readability or performance when systems grow beyond simple examples. Today, I\'m excited to introduce you to Snitch: a Kotlin HTTP framework that prioritizes readability and maintainability while delivering exceptional performance and a powerful feature set.","date":"2022-02-18T00:00:00.000Z","tags":[{"inline":false,"label":"Snitch","permalink":"/snitch/blog/tags/snitch","description":"Posts about the Snitch HTTP framework for Kotlin"},{"inline":false,"label":"Web Development","permalink":"/snitch/blog/tags/web-development","description":"Articles related to web development technologies and approaches"},{"inline":false,"label":"Kotlin","permalink":"/snitch/blog/tags/kotlin","description":"Posts about the Kotlin programming language and its ecosystem"},{"inline":false,"label":"APIs","permalink":"/snitch/blog/tags/apis","description":"Content discussing API design, implementation, and best practices"},{"inline":false,"label":"DSL","permalink":"/snitch/blog/tags/dsl","description":"Articles about Domain Specific Languages and their applications"}],"readingTime":5.395,"hasTruncateMarker":true,"authors":[{"name":"Snitch Team","title":"Snitch Framework Maintainers","url":"https://github.com/memoizr/snitch","page":{"permalink":"/snitch/blog/authors/snitch-team"},"imageURL":"https://github.com/memoizr.png","key":"snitch-team"}],"frontMatter":{"slug":"unlocking-advanced-web-apis-with-snitch","title":"Unlocking Advanced Web APIs with Snitch","authors":["snitch-team"],"tags":["snitch","web-development","kotlin","apis","dsl"]},"unlisted":false,"prevItem":{"title":"The Inspiration Behind Snitch - Borrowing from the Best","permalink":"/snitch/blog/the-inspiration-behind-snitch"},"nextItem":{"title":"Building Lightweight Microservices with Snitch","permalink":"/snitch/blog/lightweight-microservices-with-snitch"}}')},8453:(e,n,t)=>{t.d(n,{R:()=>o,x:()=>r});var i=t(6540);const s={},a=i.createContext(s);function o(e){const n=i.useContext(a);return i.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function r(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(s):e.components||s:o(e.components),i.createElement(a.Provider,{value:n},e.children)}}}]);