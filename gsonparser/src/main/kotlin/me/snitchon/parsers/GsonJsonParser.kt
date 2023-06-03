package me.snitchon.parsers

import Parser
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.snitch.Sealed
import me.snitchon.parsers.GsonJsonParser.jsonString
import java.lang.reflect.Type

class SealedAdapter : JsonDeserializer<Sealed> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Sealed {
        val type = json?.asJsonObject?.get("\$type")?.jsonString
        val rawType = TypeToken.get(typeOfT).rawType

        return rawType.kotlin.sealedSubclasses
            .firstOrNull { it.simpleName == type?.replace("\"", "") }
            ?.let {
                Gson().fromJson(json, it.java) as Sealed
            } ?: Gson().fromJson(json, rawType) as Sealed
    }
}


object GsonJsonParser : Parser {
    val builder = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        .registerTypeAdapterFactory(NullableTypAdapterFactory())
        .registerTypeHierarchyAdapter(Sealed::class.java, SealedAdapter())
    val gson = builder.create()

    override val Any.jsonString get() = gson.toJson(this)

    override val Any.jsonByteArray: ByteArray
        get() = TODO("Not yet implemented")

    override fun <T : Any> String.parseJson(klass: Class<T>): T {
        return gson.fromJson(this, klass)
    }

    inline fun <reified T : Any> String.parseJson(): T {
        return gson.fromJson(this, T::class.java)
    }

    override fun <T : Any> ByteArray.parseJson(klass: Class<T>): T = TODO()
}