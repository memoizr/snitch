package snitch.parsers

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import snitch.parsers.GsonJsonParser.serialized
import snitch.types.Parser
import snitch.types.Sealed
import java.lang.reflect.Type

class SealedAdapter : JsonDeserializer<Sealed> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Sealed {
        val type = json?.asJsonObject?.get("\$type")?.serialized
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

    override val Any.serialized get() = gson.toJson(this)

    override val Any.serializedBytes: ByteArray
        get() = TODO("Not yet implemented")

    override fun <T : Any> String.parse(klass: Class<T>): T {
        return try {
            gson.fromJson(this, klass)
        } catch (e: JsonSyntaxException) {
            throw ParsingException(e)
        }
    }

    inline fun <reified T : Any> String.parseJson(): T {
        return try {
            gson.fromJson(this, T::class.java)
        } catch (e: JsonSyntaxException) {
            throw ParsingException(e)
        }
    }

    override fun <T : Any> ByteArray.parse(klass: Class<T>): T = try {
        gson.fromJson(JsonReader(this.inputStream().bufferedReader()), klass)
    } catch (e: JsonSyntaxException) {
        throw ParsingException(e)
    }
}