package me.snitchon.router

import me.snitchon.request.RequestWrapper
import me.snitchon.response.HttpResponse
import me.snitchon.service.DecoratedWrapper
import me.snitchon.service.Endpoint

typealias Routes = Router.() -> Unit
typealias Decoration = DecoratedWrapper.() -> HttpResponse<*, *>
typealias BeforeAction =  RequestWrapper.() -> Unit
typealias AfterAction = RequestWrapper.(HttpResponse<out Any, *>) -> Unit
typealias EndpointMap = Endpoint<*>.() -> Endpoint<*>
