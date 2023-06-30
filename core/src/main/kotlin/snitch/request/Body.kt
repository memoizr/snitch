package snitch.request

import snitch.types.ContentType
import kotlin.reflect.KClass

data class Body<T : Any>(val klass: KClass<T>, val contentType: ContentType = ContentType.APPLICATION_JSON)