package me.snitchon.types

sealed class StatusCodes(val code: Int = 200) {
    object CONTINUE : StatusCodes(100)
    object SWITCHING_PROTOCOLS : StatusCodes(101)
    object OK : StatusCodes(200)
    object CREATED : StatusCodes(201)
    object ACCEPTED : StatusCodes(202)
    object NON_AUTHORITATIVE_INFORMATION : StatusCodes(203)
    object NO_CONTENT : StatusCodes(204)
    object RESET_CONTENT : StatusCodes(205)
    object PARTIAL_CONTENT : StatusCodes(206)
    object MULTIPLE_CHOICES : StatusCodes(300)
    object MOVED_PERMANENTLY : StatusCodes(301)
    object FOUND : StatusCodes(302)
    object SEE_OTHER : StatusCodes(303)
    object NOT_MODIFIED : StatusCodes(304)
    object USE_PROXY : StatusCodes(305)
    object TEMPORARY_REDIRECT : StatusCodes(307)
    object BAD_REQUEST : StatusCodes(400)
    object UNAUTHORIZED : StatusCodes(401)
    object PAYMENT_REQUIRED : StatusCodes(402)
    object FORBIDDEN : StatusCodes(403)
    object NOT_FOUND : StatusCodes(404)
    object METHOD_NOT_ALLOWED : StatusCodes(405)
    object NOT_ACCEPTABLE : StatusCodes(406)
    object PROXY_AUTHENTICATION_REQUIRED : StatusCodes(407)
    object REQUEST_TIMEOUT : StatusCodes(408)
    object CONFLICT : StatusCodes(409)
    object GONE : StatusCodes(410)
    object LENGTH_REQUIRED : StatusCodes(411)
    object PRECONDITION_FAILED : StatusCodes(412)
    object PAYLOAD_TOO_LARGE : StatusCodes(413)
    object URI_TOO_LONG : StatusCodes(414)
    object UNSUPPORTED_MEDIA_TYPE : StatusCodes(415)
    object REQUESTED_RANGE_NOT_SATISFIABLE : StatusCodes(416)
    object EXPECTATION_FAILED : StatusCodes(417)
    object INTERNAL_SERVER_ERROR : StatusCodes(500)
    object NOT_IMPLEMENTED : StatusCodes(501)
    object BAD_GATEWAY : StatusCodes(502)
    object SERVICE_UNAVAILABLE : StatusCodes(503)
    object GATEWAY_TIMEOUT : StatusCodes(504)
    object HTTP_VERSION_NOT_SUPPORTED : StatusCodes(505)
}