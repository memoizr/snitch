package snitch.router

import snitch.response.HttpResponse
import snitch.service.Endpoint
import snitch.request.RequestWrapper
import snitch.service.DecoratedWrapper

typealias Routes = Router.() -> Unit
typealias Decoration = DecoratedWrapper.() -> HttpResponse<*, *>
typealias BeforeAction =  RequestWrapper.() -> Unit
typealias AfterAction = RequestWrapper.(HttpResponse<out Any, *>) -> Unit
typealias EndpointMap = Endpoint<*>.() -> Endpoint<*>
