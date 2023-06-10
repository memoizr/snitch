## Snitch

An HTTP library for Kotlin.

- Typesafe DSL
- First-class automated OpenApi 3.0 documentation
- Really lightweight and high performance

#### How to use it

```Kotlin
    UndertowSnitchService(GsonJsonParser)
        .setRoutes {
            GET("/hello").isHandledBy { "world".ok }
        }
        .startListening()
        .generateDocumentation()
        .servePublicDocumenation()
```
    