package me.snitch.service

import me.snitch.request.RequestWrapper
import me.snitch.response.HttpResponse

class DecoratedWrapper(
    val next: () -> HttpResponse<out Any, *>,
    val wrap: RequestWrapper
): RequestWrapper by wrap
