"use strict";(self.webpackChunkguides=self.webpackChunkguides||[]).push([[1896],{7204:(e,n,i)=>{i.r(n),i.d(n,{assets:()=>o,contentTitle:()=>l,default:()=>h,frontMatter:()=>a,metadata:()=>r,toc:()=>d});const r=JSON.parse('{"id":"in depth/Anatomy-of-Validators","title":"Validator DSL","description":"Validators are a cornerstone of Snitch\'s design, ensuring that HTTP inputs are properly validated and transformed into domain types. This guide explores the internal workings of the validator DSL, explaining each component and how they fit together.","source":"@site/docs/in depth/Anatomy-of-Validators.md","sourceDirName":"in depth","slug":"/in depth/Anatomy-of-Validators","permalink":"/docs/in depth/Anatomy-of-Validators","draft":false,"unlisted":false,"editUrl":"https://github.com/memoizr/snitch/tree/master/guides/docs/in depth/Anatomy-of-Validators.md","tags":[],"version":"current","frontMatter":{},"sidebar":"tutorialSidebar","previous":{"title":"Handler DSL","permalink":"/docs/in depth/Anatomy-of-Handlers"},"next":{"title":"In-Depth: Database Integration with Exposed","permalink":"/docs/in depth/Database-Integration-With-Exposed"}}');var t=i(4848),s=i(8453);const a={},l="Validator DSL",o={},d=[{value:"The Validator Interface",id:"the-validator-interface",level:2},{value:"Creating Validators",id:"creating-validators",level:2},{value:"The <code>validator</code> Function",id:"the-validator-function",level:3},{value:"The <code>stringValidator</code> Function",id:"the-stringvalidator-function",level:3},{value:"The <code>validatorMulti</code> Function",id:"the-validatormulti-function",level:3},{value:"The <code>stringValidatorMulti</code> Function",id:"the-stringvalidatormulti-function",level:3},{value:"How Validators Work",id:"how-validators-work",level:2},{value:"Regex Validation",id:"regex-validation",level:3},{value:"Transformation Logic",id:"transformation-logic",level:3},{value:"Error Handling",id:"error-handling",level:3},{value:"The Parser&#39;s Role",id:"the-parsers-role",level:2},{value:"Custom Validators",id:"custom-validators",level:2},{value:"Validator Internals",id:"validator-internals",level:2},{value:"Best Practices",id:"best-practices",level:2},{value:"Putting It All Together",id:"putting-it-all-together",level:2},{value:"Conclusion",id:"conclusion",level:2}];function c(e){const n={code:"code",h1:"h1",h2:"h2",h3:"h3",header:"header",li:"li",ol:"ol",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,s.R)(),...e.components};return(0,t.jsxs)(t.Fragment,{children:[(0,t.jsx)(n.header,{children:(0,t.jsx)(n.h1,{id:"validator-dsl",children:"Validator DSL"})}),"\n",(0,t.jsx)(n.p,{children:"Validators are a cornerstone of Snitch's design, ensuring that HTTP inputs are properly validated and transformed into domain types. This guide explores the internal workings of the validator DSL, explaining each component and how they fit together."}),"\n",(0,t.jsx)(n.h2,{id:"the-validator-interface",children:"The Validator Interface"}),"\n",(0,t.jsxs)(n.p,{children:["At the heart of the validation system is the ",(0,t.jsx)(n.code,{children:"Validator"})," interface:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:"interface Validator<T, R> {\n    val regex: Regex\n    val description: String\n    val parse: Parser.(Collection<String>) -> R\n    fun optional(): Validator<T?, R?> = this as Validator<T?, R?>\n}\n"})}),"\n",(0,t.jsx)(n.p,{children:"Let's break down each component:"}),"\n",(0,t.jsxs)(n.ol,{children:["\n",(0,t.jsxs)(n.li,{children:["\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Type Parameters"}),":"]}),"\n",(0,t.jsxs)(n.ul,{children:["\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.code,{children:"T"}),": The input type that the validator accepts (typically ",(0,t.jsx)(n.code,{children:"String"}),")"]}),"\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.code,{children:"R"}),": The output type that the validator produces (your domain type)"]}),"\n"]}),"\n"]}),"\n",(0,t.jsxs)(n.li,{children:["\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Properties"}),":"]}),"\n",(0,t.jsxs)(n.ul,{children:["\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.code,{children:"regex"}),": A regular expression used for initial string validation"]}),"\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.code,{children:"description"}),": A human-readable description used for documentation"]}),"\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.code,{children:"parse"}),": A function that takes a collection of strings and transforms them into the output type"]}),"\n"]}),"\n"]}),"\n",(0,t.jsxs)(n.li,{children:["\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Methods"}),":"]}),"\n",(0,t.jsxs)(n.ul,{children:["\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.code,{children:"optional()"}),": Converts a required validator to an optional one"]}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,t.jsx)(n.p,{children:"The interface is intentionally minimal, focusing on the essential components of validation: pattern matching, transformation, and documentation."}),"\n",(0,t.jsx)(n.h2,{id:"creating-validators",children:"Creating Validators"}),"\n",(0,t.jsx)(n.p,{children:"Snitch provides several factory functions for creating validators with different behaviors:"}),"\n",(0,t.jsxs)(n.h3,{id:"the-validator-function",children:["The ",(0,t.jsx)(n.code,{children:"validator"})," Function"]}),"\n",(0,t.jsx)(n.p,{children:"The most general factory function:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'inline fun <From, To> validator(\n    descriptions: String,\n    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),\n    crossinline mapper: Parser.(String) -> To\n) = object : Validator<From, To> {\n    override val description = descriptions\n    override val regex = regex\n    override val parse: Parser.(Collection<String>) -> To = { mapper(it.single()) }\n}\n'})}),"\n",(0,t.jsx)(n.p,{children:"This function creates a validator that:"}),"\n",(0,t.jsxs)(n.ul,{children:["\n",(0,t.jsx)(n.li,{children:"Has a custom description"}),"\n",(0,t.jsx)(n.li,{children:"Uses a specified regex (or a default that matches any non-empty string)"}),"\n",(0,t.jsx)(n.li,{children:"Applies a mapping function to transform the input"}),"\n"]}),"\n",(0,t.jsxs)(n.p,{children:["The ",(0,t.jsx)(n.code,{children:"crossinline"})," modifier ensures that the mapper function can be used inside a lambda that will be inlined."]}),"\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Typical Usage"}),":"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'val ofUUID = validator<String, UUID>(\n    "valid UUID",\n    """^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$""".toRegex(RegexOption.IGNORE_CASE)\n) {\n    try {\n        UUID.fromString(it)\n    } catch (e: IllegalArgumentException) {\n        throw IllegalArgumentException("Invalid UUID format")\n    }\n}\n'})}),"\n",(0,t.jsxs)(n.h3,{id:"the-stringvalidator-function",children:["The ",(0,t.jsx)(n.code,{children:"stringValidator"})," Function"]}),"\n",(0,t.jsx)(n.p,{children:"A specialized version for string inputs:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'inline fun <To> stringValidator(\n    description: String = "",\n    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),\n    crossinline mapper: Parser.(String) -> To,\n) = validator<String, To>(description, regex, mapper)\n'})}),"\n",(0,t.jsxs)(n.p,{children:["This is a convenience function that defaults the input type to ",(0,t.jsx)(n.code,{children:"String"}),", which is the most common case."]}),"\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Typical Usage"}),":"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'val ofEmail = stringValidator(\n    "email address",\n    """^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$""".toRegex()\n) { it }\n'})}),"\n",(0,t.jsxs)(n.h3,{id:"the-validatormulti-function",children:["The ",(0,t.jsx)(n.code,{children:"validatorMulti"})," Function"]}),"\n",(0,t.jsx)(n.p,{children:"For handling collections of values:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'fun <From, To> validatorMulti(\n    descriptions: String,\n    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),\n    mapper: Parser.(Collection<String>) -> To\n) = object : Validator<From, To> {\n    override val description = descriptions\n    override val regex = regex\n    override val parse: Parser.(Collection<String>) -> To = mapper\n}\n'})}),"\n",(0,t.jsx)(n.p,{children:"This function allows working with multiple input values, such as repeated query parameters."}),"\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Typical Usage"}),":"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'val ofStringSet = validatorMulti<String, Set<String>>(\n    "set of strings"\n) { strings ->\n    strings.flatMap { it.split(",") }.toSet()\n}\n'})}),"\n",(0,t.jsxs)(n.h3,{id:"the-stringvalidatormulti-function",children:["The ",(0,t.jsx)(n.code,{children:"stringValidatorMulti"})," Function"]}),"\n",(0,t.jsx)(n.p,{children:"A specialized version for string inputs that return collections:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'fun <To> stringValidatorMulti(\n    description: String,\n    regex: Regex = """^.+$""".toRegex(RegexOption.DOT_MATCHES_ALL),\n    mapper: Parser.(Collection<String>) -> To,\n) = validatorMulti<String, To>(description, regex, mapper)\n'})}),"\n",(0,t.jsxs)(n.p,{children:["This combines the convenience of ",(0,t.jsx)(n.code,{children:"stringValidator"})," with the collection handling of ",(0,t.jsx)(n.code,{children:"validatorMulti"}),"."]}),"\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Typical Usage"}),":"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'val ofTags = stringValidatorMulti<List<String>>(\n    "comma-separated tags"\n) { params ->\n    params.flatMap { it.split(",") }\n        .map { it.trim() }\n        .filter { it.isNotEmpty() }\n}\n'})}),"\n",(0,t.jsx)(n.h2,{id:"how-validators-work",children:"How Validators Work"}),"\n",(0,t.jsx)(n.p,{children:"Now that we understand the interface and creation functions, let's explore how validators operate at runtime."}),"\n",(0,t.jsx)(n.h3,{id:"regex-validation",children:"Regex Validation"}),"\n",(0,t.jsxs)(n.p,{children:["The first step in validation is pattern matching using the ",(0,t.jsx)(n.code,{children:"regex"})," property:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'// Inside parameter handler code\nif (!validator.regex.matches(value)) {\n    throw ValidationException("Value doesn\'t match pattern for ${validator.description}")\n}\n'})}),"\n",(0,t.jsx)(n.p,{children:"This provides a fast first-pass validation before more complex logic is applied. For example, checking that an email string has a basic email-like structure before attempting further validation."}),"\n",(0,t.jsx)(n.h3,{id:"transformation-logic",children:"Transformation Logic"}),"\n",(0,t.jsxs)(n.p,{children:["After regex validation passes, the ",(0,t.jsx)(n.code,{children:"parse"})," function is called with the collection of parameter values:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'// Inside parameter handler code\ntry {\n    return validator.parse(parser, values)\n} catch (e: Exception) {\n    throw ValidationException("Failed to parse ${validator.description}: ${e.message}")\n}\n'})}),"\n",(0,t.jsx)(n.p,{children:"The parse function is responsible for:"}),"\n",(0,t.jsxs)(n.ol,{children:["\n",(0,t.jsx)(n.li,{children:"Handling single vs. multiple values"}),"\n",(0,t.jsx)(n.li,{children:"Converting strings to the target type"}),"\n",(0,t.jsx)(n.li,{children:"Performing business-specific validation"}),"\n",(0,t.jsx)(n.li,{children:"Throwing exceptions for invalid inputs"}),"\n"]}),"\n",(0,t.jsxs)(n.p,{children:["The transformation typically has access to the ",(0,t.jsx)(n.code,{children:"Parser"})," instance, which provides useful utilities for working with common formats like JSON."]}),"\n",(0,t.jsx)(n.h3,{id:"error-handling",children:"Error Handling"}),"\n",(0,t.jsx)(n.p,{children:"Validators report errors by throwing exceptions, which Snitch catches and converts to appropriate HTTP responses (typically 400 Bad Request)."}),"\n",(0,t.jsx)(n.p,{children:"This happens at several levels:"}),"\n",(0,t.jsxs)(n.ol,{children:["\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.strong,{children:"Regex mismatch"}),": Throws a ",(0,t.jsx)(n.code,{children:"ValidationException"})]}),"\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.strong,{children:"Empty collection"}),": Throws a ",(0,t.jsx)(n.code,{children:"NoSuchElementException"})," from the ",(0,t.jsx)(n.code,{children:"single()"})," call"]}),"\n",(0,t.jsxs)(n.li,{children:[(0,t.jsx)(n.strong,{children:"Custom validation"}),": Validator-specific exceptions from the mapper function"]}),"\n"]}),"\n",(0,t.jsx)(n.p,{children:"Snitch provides automatic handling for all of these, generating clear error messages for API consumers."}),"\n",(0,t.jsx)(n.h2,{id:"the-parsers-role",children:"The Parser's Role"}),"\n",(0,t.jsxs)(n.p,{children:["You may have noticed that the validator functions all pass a ",(0,t.jsx)(n.code,{children:"Parser"})," instance to the mapper function. The ",(0,t.jsx)(n.code,{children:"Parser"})," is an interface for converting between strings and structured data:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:"interface Parser {\n    fun <T> fromJson(json: String): T\n    fun <T> toJson(value: T): String\n    fun <T : Enum<T>> String.parse(enumClass: Class<T>): T\n}\n"})}),"\n",(0,t.jsx)(n.p,{children:"This allows validators to leverage the application's JSON parser for complex transformations, particularly for request bodies."}),"\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Example using the Parser"}),":"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'val ofUser = stringValidator<User>("user") {\n    parser.fromJson<User>(it)\n}\n'})}),"\n",(0,t.jsx)(n.p,{children:"This is particularly powerful for body validators, allowing seamless conversion between JSON strings and domain objects."}),"\n",(0,t.jsx)(n.h2,{id:"custom-validators",children:"Custom Validators"}),"\n",(0,t.jsxs)(n.p,{children:["While the factory functions cover most use cases, you can also implement the ",(0,t.jsx)(n.code,{children:"Validator"})," interface directly for complete control:"]}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'object UserIdValidator : Validator<String, UserId> {\n    override val description = "Valid user ID"\n    override val regex = """^[a-zA-Z0-9]{8,12}$""".toRegex()\n    override val parse: Parser.(Collection<String>) -> UserId = { collection ->\n        val value = collection.first()\n        // Custom validation logic\n        if (!userRepository.exists(value)) {\n            throw IllegalArgumentException("User ID does not exist")\n        }\n        UserId(value)\n    }\n}\n'})}),"\n",(0,t.jsx)(n.p,{children:"This approach is useful when:"}),"\n",(0,t.jsxs)(n.ul,{children:["\n",(0,t.jsx)(n.li,{children:"You need complex validation logic"}),"\n",(0,t.jsx)(n.li,{children:"You want to encapsulate validation in a self-contained object"}),"\n",(0,t.jsx)(n.li,{children:"You need to inject dependencies (like repositories) into the validator"}),"\n"]}),"\n",(0,t.jsx)(n.h2,{id:"validator-internals",children:"Validator Internals"}),"\n",(0,t.jsx)(n.p,{children:"Let's explore what happens when a validator is used with a parameter:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:"val userId by path(ofUUID)\n"})}),"\n",(0,t.jsx)(n.p,{children:"Here's the sequence of events:"}),"\n",(0,t.jsxs)(n.ol,{children:["\n",(0,t.jsxs)(n.li,{children:["The ",(0,t.jsx)(n.code,{children:"path"})," function creates a ",(0,t.jsx)(n.code,{children:"Parameter"})," object, storing the validator"]}),"\n",(0,t.jsx)(n.li,{children:"When a request arrives, Snitch extracts the raw path parameter value"}),"\n",(0,t.jsx)(n.li,{children:"The validator's regex is checked against the value"}),"\n",(0,t.jsx)(n.li,{children:"If the regex matches, the parse function is called"}),"\n",(0,t.jsx)(n.li,{children:"The parse function converts the string to a UUID"}),"\n",(0,t.jsxs)(n.li,{children:["The result is cached and made available via ",(0,t.jsx)(n.code,{children:"request[userId]"})]}),"\n"]}),"\n",(0,t.jsx)(n.p,{children:"If any step fails, the request processing is halted, and an error response is returned to the client."}),"\n",(0,t.jsx)(n.h2,{id:"best-practices",children:"Best Practices"}),"\n",(0,t.jsx)(n.p,{children:"Based on the internal workings of validators, here are some best practices:"}),"\n",(0,t.jsxs)(n.ol,{children:["\n",(0,t.jsxs)(n.li,{children:["\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Use specific regex patterns"}),": The more specific your regex, the faster you can reject invalid inputs"]}),"\n"]}),"\n",(0,t.jsxs)(n.li,{children:["\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Keep transformation functions pure"}),": Avoid side effects in mapper functions for easier testing and reasoning"]}),"\n"]}),"\n",(0,t.jsxs)(n.li,{children:["\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Provide clear error messages"}),": When throwing exceptions, include specific details about why validation failed"]}),"\n"]}),"\n",(0,t.jsxs)(n.li,{children:["\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Define domain-specific validators"}),": Create validators for your domain types to encapsulate validation logic"]}),"\n"]}),"\n",(0,t.jsxs)(n.li,{children:["\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Compose validators"}),": Build complex validators by combining simpler ones"]}),"\n"]}),"\n",(0,t.jsxs)(n.li,{children:["\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Avoid heavy computation in validators"}),": Validators run on every request, so keep them efficient"]}),"\n"]}),"\n",(0,t.jsxs)(n.li,{children:["\n",(0,t.jsxs)(n.p,{children:[(0,t.jsx)(n.strong,{children:"Use the optional() method"}),": For truly optional parameters, apply ",(0,t.jsx)(n.code,{children:"optional()"})," to your validator instead of handling nullability in mapper functions"]}),"\n"]}),"\n"]}),"\n",(0,t.jsx)(n.h2,{id:"putting-it-all-together",children:"Putting It All Together"}),"\n",(0,t.jsx)(n.p,{children:"Let's see a complete example of a custom validator used in an endpoint:"}),"\n",(0,t.jsx)(n.pre,{children:(0,t.jsx)(n.code,{className:"language-kotlin",children:'// Domain type\ndata class UserId(val value: String)\n\n// Custom validator\nval ofUserId = validator<String, UserId>(\n    "valid user ID",\n    """^[a-zA-Z0-9]{8,12}$""".toRegex()\n) {\n    if (it.length < 8 || it.length > 12) {\n        throw IllegalArgumentException("User ID must be 8-12 characters long")\n    }\n    \n    if (!it.matches("""^[a-zA-Z0-9]*$""".toRegex())) {\n        throw IllegalArgumentException("User ID must contain only letters and numbers")\n    }\n    \n    UserId(it)\n}\n\n// Parameter definition\nval userId by path(ofUserId)\n\n// Route with validated parameter\nGET("users" / userId) isHandledBy {\n    // UserId is already validated and transformed\n    val id: UserId = request[userId]\n    userRepository.findById(id).ok\n}\n'})}),"\n",(0,t.jsx)(n.p,{children:"This approach ensures:"}),"\n",(0,t.jsxs)(n.ol,{children:["\n",(0,t.jsx)(n.li,{children:"Early validation at the HTTP layer"}),"\n",(0,t.jsx)(n.li,{children:"Type-safe access to domain types"}),"\n",(0,t.jsx)(n.li,{children:"Clean separation of validation and business logic"}),"\n",(0,t.jsx)(n.li,{children:"Clear error messages for API consumers"}),"\n"]}),"\n",(0,t.jsx)(n.h2,{id:"conclusion",children:"Conclusion"}),"\n",(0,t.jsx)(n.p,{children:"The validator DSL in Snitch provides a powerful, type-safe way to transform raw HTTP inputs into domain types. By understanding its internal workings, you can create more robust, maintainable APIs with clear error handling and strong type safety."}),"\n",(0,t.jsx)(n.p,{children:"Remember that validators aren't just about rejecting invalid inputs\u2014they're about bridging the gap between the untyped world of HTTP and the strongly-typed world of your domain model."})]})}function h(e={}){const{wrapper:n}={...(0,s.R)(),...e.components};return n?(0,t.jsx)(n,{...e,children:(0,t.jsx)(c,{...e})}):c(e)}},8453:(e,n,i)=>{i.d(n,{R:()=>a,x:()=>l});var r=i(6540);const t={},s=r.createContext(t);function a(e){const n=r.useContext(s);return r.useMemo((function(){return"function"==typeof e?e(n):{...n,...e}}),[n,e])}function l(e){let n;return n=e.disableParentContext?"function"==typeof e.components?e.components(t):e.components||t:a(e.components),r.createElement(s.Provider,{value:n},e.children)}}}]);