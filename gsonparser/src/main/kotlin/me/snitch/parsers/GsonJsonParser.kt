package me.snitch.parsers

import me.snitch.parsing.Parser
import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import me.snitch.types.Sealed
import me.snitch.parsers.GsonJsonParser.serialized
import me.snitch.parsing.ParsingException
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