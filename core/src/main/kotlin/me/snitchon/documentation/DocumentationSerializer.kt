package me.snitchon.documentation

import kotlin.reflect.KClass
import kotlin.reflect.KParameter

interface DocumentationSerializer {
    fun serializeField(param: KParameter, klass: KClass<*>): String
}

object DefaultDocumentatoinSerializer : DocumentationSerializer {
    override fun serializeField(param: KParameter, klass: KClass<*>): String = param.name!!
}
