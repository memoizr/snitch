interface Parser {
    val Any.jsonString: String
    val Any.jsonByteArray: ByteArray

    fun <T: Any> String.parseJson(klass: Class<T>): T
    fun <T: Any> ByteArray.parseJson(klass: Class<T>): T
}

class ParsingException(message: String): Exception(message)