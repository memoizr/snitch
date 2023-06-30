package snitch.service

import snitch.request.RequestWrapper
import snitch.response.HttpResponse

class DecoratedWrapper(
    val next: () -> HttpResponse<out Any, *>,
    val wrap: RequestWrapper
): RequestWrapper by wrap
