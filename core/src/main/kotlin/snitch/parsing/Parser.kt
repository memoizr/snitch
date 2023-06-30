package snitch.parsing

interface Parser {
    val Any.serialized: String
    val Any.serializedBytes: ByteArray

    fun <T: Any> String.parse(klass: Class<T>): T
    fun <T: Any> ByteArray.parse(klass: Class<T>): T
}
