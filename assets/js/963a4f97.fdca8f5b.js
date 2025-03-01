"use strict";(self.webpackChunkguides=self.webpackChunkguides||[]).push([[5440],{4525:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>d,contentTitle:()=>a,default:()=>u,frontMatter:()=>r,metadata:()=>s,toc:()=>c});const s=JSON.parse('{"id":"tutorials/Mastering-Snitch-Conditions","title":"Mastering Snitch Conditions","description":"Conditions are one of Snitch\'s most powerful features, allowing you to implement sophisticated access control and request validation with minimal code. This tutorial will guide you through everything you need to know about conditions, from basic usage to advanced patterns.","source":"@site/docs/tutorials/Mastering-Snitch-Conditions.md","sourceDirName":"tutorials","slug":"/tutorials/Mastering-Snitch-Conditions","permalink":"/docs/tutorials/Mastering-Snitch-Conditions","draft":false,"unlisted":false,"editUrl":"https://github.com/memoizr/snitch/tree/master/guides/docs/tutorials/Mastering-Snitch-Conditions.md","tags":[],"version":"current","frontMatter":{},"sidebar":"tutorialSidebar","previous":{"title":"Mastering Snitch Before and After Actions","permalink":"/docs/tutorials/Mastering-Snitch-BeforeAfter"},"next":{"title":"Mastering Snitch Decorations","permalink":"/docs/tutorials/Mastering-Snitch-Decorations"}}');var o=i(4848),t=i(8453);const r={},a="Mastering Snitch Conditions",d={},c=[{value:"Understanding Conditions",id:"understanding-conditions",level:2},{value:"Basic Condition Usage",id:"basic-condition-usage",level:2},{value:"Creating Custom Conditions",id:"creating-custom-conditions",level:2},{value:"Parameterized Conditions",id:"parameterized-conditions",level:3},{value:"Logical Operators",id:"logical-operators",level:2},{value:"AND (<code>and</code>)",id:"and-and",level:3},{value:"OR (<code>or</code>)",id:"or-or",level:3},{value:"NOT (<code>not</code> or <code>!</code>)",id:"not-not-or-",level:3},{value:"Applying Conditions to Route Hierarchies",id:"applying-conditions-to-route-hierarchies",level:2},{value:"Short-Circuit Evaluation",id:"short-circuit-evaluation",level:2},{value:"Error Handling and Custom Responses",id:"error-handling-and-custom-responses",level:2},{value:"Best Practices",id:"best-practices",level:2},{value:"1. Keep Conditions Focused",id:"1-keep-conditions-focused",level:3},{value:"2. Use Descriptive Names",id:"2-use-descriptive-names",level:3},{value:"3. Leverage Composition",id:"3-leverage-composition",level:3},{value:"4. Provide Helpful Error Messages",id:"4-provide-helpful-error-messages",level:3},{value:"5. Document Conditions",id:"5-document-conditions",level:3},{value:"Real-World Examples",id:"real-world-examples",level:2},{value:"Authentication and Authorization",id:"authentication-and-authorization",level:3},{value:"Rate Limiting",id:"rate-limiting",level:3},{value:"Feature Flags",id:"feature-flags",level:3}];function l(e){const n={code:"code",h1:"h1",h2:"h2",h3:"h3",header:"header",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,t.R)(),...e.components};return(0,o.jsxs)(o.Fragment,{children:[(0,o.jsx)(n.header,{children:(0,o.jsx)(n.h1,{id:"mastering-snitch-conditions",children:"Mastering Snitch Conditions"})}),"\n",(0,o.jsx)(n.p,{children:"Conditions are one of Snitch's most powerful features, allowing you to implement sophisticated access control and request validation with minimal code. This tutorial will guide you through everything you need to know about conditions, from basic usage to advanced patterns."}),"\n",(0,o.jsx)(n.h2,{id:"understanding-conditions",children:"Understanding Conditions"}),"\n",(0,o.jsxs)(n.p,{children:["In Snitch, a condition is a predicate that evaluates a request and determines whether it should proceed or be rejected. Conditions are represented by the ",(0,o.jsx)(n.code,{children:"Condition"})," interface, which has three key components:"]}),"\n",(0,o.jsxs)(n.ol,{children:["\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Description"}),": A human-readable description of what the condition checks"]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Transform function"}),": A function that can modify an endpoint (usually for documentation purposes)"]}),"\n",(0,o.jsxs)(n.li,{children:[(0,o.jsx)(n.strong,{children:"Check function"}),": The actual logic that evaluates the request"]}),"\n"]}),"\n",(0,o.jsxs)(n.p,{children:["When a condition is applied to an endpoint using ",(0,o.jsx)(n.code,{children:"onlyIf"}),", it becomes part of the request processing pipeline. If the condition evaluates to ",(0,o.jsx)(n.code,{children:"Successful"}),", the request proceeds; if it evaluates to ",(0,o.jsx)(n.code,{children:"Failed"}),", the request is rejected with the specified error response."]}),"\n",(0,o.jsx)(n.h2,{id:"basic-condition-usage",children:"Basic Condition Usage"}),"\n",(0,o.jsxs)(n.p,{children:["The simplest way to use conditions is with the ",(0,o.jsx)(n.code,{children:"onlyIf"})," method on an endpoint:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'GET("resource" / resourceId) onlyIf isResourceOwner isHandledBy { getResource() }\n'})}),"\n",(0,o.jsxs)(n.p,{children:["This ensures that the endpoint will only be accessible if the ",(0,o.jsx)(n.code,{children:"isResourceOwner"})," condition evaluates to ",(0,o.jsx)(n.code,{children:"Successful"}),"."]}),"\n",(0,o.jsx)(n.h2,{id:"creating-custom-conditions",children:"Creating Custom Conditions"}),"\n",(0,o.jsxs)(n.p,{children:["You can create custom conditions using the ",(0,o.jsx)(n.code,{children:"condition"})," factory function:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'val hasAdminRole = condition("hasAdminRole") {\n    val role = (request[accessToken] as? Authentication.Authenticated)?.claims?.role\n    \n    when (role) {\n        Role.ADMIN -> ConditionResult.Successful\n        else -> ConditionResult.Failed("Admin role required".forbidden())\n    }\n}\n'})}),"\n",(0,o.jsxs)(n.p,{children:["The first parameter is the description, which will be used in documentation and error messages. The lambda receives a ",(0,o.jsx)(n.code,{children:"RequestWrapper"})," and should return a ",(0,o.jsx)(n.code,{children:"ConditionResult"}),"."]}),"\n",(0,o.jsx)(n.h3,{id:"parameterized-conditions",children:"Parameterized Conditions"}),"\n",(0,o.jsx)(n.p,{children:"You can create reusable condition factories that accept parameters:"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'fun hasMinimumAge(minAge: Int) = condition("hasMinimumAge($minAge)") {\n    val userAge = userRepository.getAge(request[userId])\n    \n    if (userAge >= minAge) {\n        ConditionResult.Successful\n    } else {\n        ConditionResult.Failed("User must be at least $minAge years old".forbidden())\n    }\n}\n\n// Usage\nGET("adult-content") onlyIf hasMinimumAge(18) isHandledBy { getAdultContent() }\n'})}),"\n",(0,o.jsx)(n.h2,{id:"logical-operators",children:"Logical Operators"}),"\n",(0,o.jsx)(n.p,{children:"Snitch conditions support three logical operators:"}),"\n",(0,o.jsxs)(n.h3,{id:"and-and",children:["AND (",(0,o.jsx)(n.code,{children:"and"}),")"]}),"\n",(0,o.jsxs)(n.p,{children:["The ",(0,o.jsx)(n.code,{children:"and"})," operator creates a condition that succeeds only if both conditions succeed:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:"val canAccessResource = isAuthenticated and hasPermission\n"})}),"\n",(0,o.jsxs)(n.p,{children:["When evaluating an ",(0,o.jsx)(n.code,{children:"and"})," condition, if the first condition fails, the second one is not evaluated (short-circuit evaluation)."]}),"\n",(0,o.jsxs)(n.h3,{id:"or-or",children:["OR (",(0,o.jsx)(n.code,{children:"or"}),")"]}),"\n",(0,o.jsxs)(n.p,{children:["The ",(0,o.jsx)(n.code,{children:"or"})," operator creates a condition that succeeds if either condition succeeds:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:"val canModifyResource = isResourceOwner or hasAdminRole\n"})}),"\n",(0,o.jsxs)(n.p,{children:["When evaluating an ",(0,o.jsx)(n.code,{children:"or"})," condition, if the first condition succeeds, the second one is not evaluated."]}),"\n",(0,o.jsxs)(n.h3,{id:"not-not-or-",children:["NOT (",(0,o.jsx)(n.code,{children:"not"})," or ",(0,o.jsx)(n.code,{children:"!"}),")"]}),"\n",(0,o.jsxs)(n.p,{children:["The ",(0,o.jsx)(n.code,{children:"not"})," operator inverts a condition:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:"val isNotLocked = !isResourceLocked\n"})}),"\n",(0,o.jsx)(n.p,{children:"You can combine these operators to create complex access rules:"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:"val canEditDocument = isAuthenticated and (isDocumentOwner or hasEditorRole) and !isDocumentLocked\n"})}),"\n",(0,o.jsx)(n.h2,{id:"applying-conditions-to-route-hierarchies",children:"Applying Conditions to Route Hierarchies"}),"\n",(0,o.jsxs)(n.p,{children:["You can apply conditions to entire route hierarchies using the ",(0,o.jsx)(n.code,{children:"onlyIf"})," block:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'onlyIf(isAuthenticated) {\n    GET("profile") isHandledBy { getProfile() }\n    \n    onlyIf(hasAdminRole) {\n        GET("admin/dashboard") isHandledBy { getDashboard() }\n        GET("admin/users") isHandledBy { getUsers() }\n    }\n}\n'})}),"\n",(0,o.jsx)(n.p,{children:"In this example, all routes require authentication, and the admin routes additionally require the admin role."}),"\n",(0,o.jsx)(n.h2,{id:"short-circuit-evaluation",children:"Short-Circuit Evaluation"}),"\n",(0,o.jsx)(n.p,{children:"Snitch's condition operators use short-circuit evaluation for efficiency:"}),"\n",(0,o.jsxs)(n.ul,{children:["\n",(0,o.jsxs)(n.li,{children:["For ",(0,o.jsx)(n.code,{children:"and"}),", if the first condition fails, the second is not evaluated"]}),"\n",(0,o.jsxs)(n.li,{children:["For ",(0,o.jsx)(n.code,{children:"or"}),", if the first condition succeeds, the second is not evaluated"]}),"\n"]}),"\n",(0,o.jsx)(n.p,{children:"This is particularly useful when you have conditions with side effects or expensive operations:"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:"// The database query will only run if the user is authenticated\nval canAccessResource = isAuthenticated and hasPermissionInDatabase\n"})}),"\n",(0,o.jsx)(n.p,{children:"You can test this behavior:"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'@Test\nfun `short-circuits condition evaluation`() {\n    var secondConditionEvaluated = false\n    \n    val trackingCondition = condition("tracking") {\n        secondConditionEvaluated = true\n        ConditionResult.Successful\n    }\n    \n    given {\n        GET("short-circuit")\n            .onlyIf(alwaysFalse and trackingCondition)\n            .isHandledBy { "".ok }\n    } then {\n        GET("/short-circuit").expectCode(403)\n        assert(!secondConditionEvaluated) { "Second condition should not have been evaluated" }\n    }\n}\n'})}),"\n",(0,o.jsx)(n.h2,{id:"error-handling-and-custom-responses",children:"Error Handling and Custom Responses"}),"\n",(0,o.jsxs)(n.p,{children:["When a condition fails, it returns a ",(0,o.jsx)(n.code,{children:"ConditionResult.Failed"})," with an error response. You can customize this response:"]}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'val isResourceOwner = condition("isResourceOwner") {\n    if (principal.id == request[resourceId]) {\n        ConditionResult.Successful\n    } else {\n        ConditionResult.Failed(\n            ErrorResponse(\n                code = "FORBIDDEN",\n                message = "You don\'t have permission to access this resource",\n                details = mapOf("resourceId" to request[resourceId])\n            ).error(StatusCodes.FORBIDDEN)\n        )\n    }\n}\n'})}),"\n",(0,o.jsx)(n.p,{children:"This allows you to provide detailed, context-specific error messages to clients."}),"\n",(0,o.jsx)(n.h2,{id:"best-practices",children:"Best Practices"}),"\n",(0,o.jsx)(n.h3,{id:"1-keep-conditions-focused",children:"1. Keep Conditions Focused"}),"\n",(0,o.jsx)(n.p,{children:"Each condition should check one specific thing. This makes them more reusable and easier to understand."}),"\n",(0,o.jsx)(n.h3,{id:"2-use-descriptive-names",children:"2. Use Descriptive Names"}),"\n",(0,o.jsx)(n.p,{children:"Choose condition names that clearly describe what they check:"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'// Good\nval hasAdminRole = condition("hasAdminRole") { ... }\n\n// Not as good\nval adminCheck = condition("adminCheck") { ... }\n'})}),"\n",(0,o.jsx)(n.h3,{id:"3-leverage-composition",children:"3. Leverage Composition"}),"\n",(0,o.jsx)(n.p,{children:"Build complex access rules by composing simple conditions:"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:"val canEditDocument = isAuthenticated and isDocumentOwner and !isDocumentLocked\n"})}),"\n",(0,o.jsx)(n.h3,{id:"4-provide-helpful-error-messages",children:"4. Provide Helpful Error Messages"}),"\n",(0,o.jsx)(n.p,{children:"When a condition fails, the error message should help the client understand why:"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'ConditionResult.Failed("Resource not found or you don\'t have permission to access it".forbidden())\n'})}),"\n",(0,o.jsx)(n.h3,{id:"5-document-conditions",children:"5. Document Conditions"}),"\n",(0,o.jsx)(n.p,{children:"Use the description parameter to document what the condition checks:"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'val hasPermission = condition("User has permission to access the resource") { ... }\n'})}),"\n",(0,o.jsx)(n.p,{children:"This description will appear in the generated API documentation."}),"\n",(0,o.jsx)(n.h2,{id:"real-world-examples",children:"Real-World Examples"}),"\n",(0,o.jsx)(n.h3,{id:"authentication-and-authorization",children:"Authentication and Authorization"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'// Authentication\nval isAuthenticated = condition("isAuthenticated") {\n    when (request[accessToken]) {\n        is Authentication.Authenticated -> ConditionResult.Successful\n        else -> ConditionResult.Failed("Authentication required".unauthorized())\n    }\n}\n\n// Authorization\nval hasAdminRole = condition("hasAdminRole") {\n    val auth = request[accessToken] as? Authentication.Authenticated\n        ?: return@condition ConditionResult.Failed("Authentication required".unauthorized())\n    \n    when (auth.claims.role) {\n        Role.ADMIN -> ConditionResult.Successful\n        else -> ConditionResult.Failed("Admin role required".forbidden())\n    }\n}\n\n// Resource ownership\nfun isResourceOwner(resourceIdParam: Parameter<String, *>) = condition("isResourceOwner") {\n    val auth = request[accessToken] as? Authentication.Authenticated\n        ?: return@condition ConditionResult.Failed("Authentication required".unauthorized())\n    \n    if (auth.claims.userId == request[resourceIdParam]) {\n        ConditionResult.Successful\n    } else {\n        ConditionResult.Failed("You don\'t own this resource".forbidden())\n    }\n}\n\n// Usage\nval routes = routes {\n    onlyIf(isAuthenticated) {\n        GET("profile") isHandledBy { getProfile() }\n        \n        "resources" / resourceId / {\n            GET() onlyIf isResourceOwner(resourceId) isHandledBy { getResource() }\n            PUT() onlyIf (isResourceOwner(resourceId) or hasAdminRole) isHandledBy { updateResource() }\n            DELETE() onlyIf (isResourceOwner(resourceId) or hasAdminRole) isHandledBy { deleteResource() }\n        }\n        \n        onlyIf(hasAdminRole) {\n            GET("admin/dashboard") isHandledBy { getDashboard() }\n            GET("admin/users") isHandledBy { getUsers() }\n        }\n    }\n}\n'})}),"\n",(0,o.jsx)(n.h3,{id:"rate-limiting",children:"Rate Limiting"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'fun rateLimit(maxRequests: Int, perTimeWindow: Duration) = condition("rateLimit($maxRequests per $perTimeWindow)") {\n    val clientIp = request.remoteAddress\n    val requestCount = rateLimiter.getRequestCount(clientIp, perTimeWindow)\n    \n    if (requestCount <= maxRequests) {\n        ConditionResult.Successful\n    } else {\n        ConditionResult.Failed(\n            ErrorResponse(\n                code = "TOO_MANY_REQUESTS",\n                message = "Rate limit exceeded. Try again later.",\n                details = mapOf(\n                    "maxRequests" to maxRequests,\n                    "timeWindow" to perTimeWindow.toString(),\n                    "retryAfter" to rateLimiter.getRetryAfter(clientIp)\n                )\n            ).error(StatusCodes.TOO_MANY_REQUESTS)\n        )\n    }\n}\n\n// Usage\nonlyIf(rateLimit(100, Duration.ofMinutes(1))) {\n    POST("api/v1/messages") isHandledBy { sendMessage() }\n}\n'})}),"\n",(0,o.jsx)(n.h3,{id:"feature-flags",children:"Feature Flags"}),"\n",(0,o.jsx)(n.pre,{children:(0,o.jsx)(n.code,{className:"language-kotlin",children:'fun featureEnabled(featureName: String) = condition("featureEnabled($featureName)") {\n    if (featureFlagService.isEnabled(featureName)) {\n        ConditionResult.Successful\n    } else {\n        ConditionResult.Failed("Feature not available".notFound())\n    }\n}\n\n// Usage\nGET("new-feature") onlyIf featureEnabled("new-feature") isHandledBy { useNewFeature() }\n'})}),"\n",(0,o.jsx)(n.p,{children:"By mastering Snitch's condition system, you can implement sophisticated access control and request validation with minimal code, keeping your routes clean and focused on business logic."})]})}function u(e={}){const{wrapper:n}={...(0,t.R)(),...e.components};return n?(0,o.jsx)(n,{...e,children:(0,o.jsx)(l,{...e})}):l(e)}},8453:(e,n,i)=>{i.d(n,{R:()=>r,x:()=>a});var s=i(6540);const o={},t=s.createContext(o);function r(e){const n=s.useContext(t);return s.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function a(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(o):e.components||o:r(e.components),s.createElement(t.Provider,{value:n},e.children)}}}]);