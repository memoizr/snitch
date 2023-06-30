package me.snitch.router

import me.snitch.request.RequestWrapper
import me.snitch.response.HttpResponse
import me.snitch.service.DecoratedWrapper
import me.snitch.service.Endpoint

typealias Routes = Router.() -> Unit
typealias Decoration = DecoratedWrapper.() -> HttpResponse<*, *>
typealias BeforeAction =  RequestWrapper.() -> Unit
typealias AfterAction = RequestWrapper.(HttpResponse<out Any, *>) -> Unit
typealias EndpointMap = Endpoint<*>.() -> Endpoint<*>
