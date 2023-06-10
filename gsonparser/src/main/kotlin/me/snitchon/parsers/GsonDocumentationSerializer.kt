package me.snitchon.parsers

import com.google.gson.annotations.SerializedName
import me.snitchon.documentation.DocumentationSerializer
import kotlin.reflect.KClass
import kotlin.reflect.KParameter

object GsonDocumentationSerializer: DocumentationSerializer {
    override fun serializeField(param: KParameter, klass: KClass<*>): String {
        val name = param.name
        val field = klass.java.declaredFields.find { it.name == name}
        val serializedName = field?.annotations?.find { it is SerializedName } as SerializedName?
        return serializedName?.value ?: serializedName?.alternate?.firstOrNull() ?: param.name!!
    }
}