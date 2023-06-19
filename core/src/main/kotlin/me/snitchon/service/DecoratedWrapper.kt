package me.snitchon.service

import me.snitchon.request.RequestWrapper
import me.snitchon.response.HttpResponse

class DecoratedWrapper(
    val next: () -> HttpResponse<*, *>,
    val wrap: RequestWrapper
): RequestWrapper by wrap
