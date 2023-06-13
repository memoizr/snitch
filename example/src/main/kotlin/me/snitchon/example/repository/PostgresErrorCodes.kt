package me.snitchon.example.repository;

enum class PostgresErrorCodes(val sqlState: String) {
    UNIQUE_VIOLATION("23505"),
    FOREIGN_KEY_VIOLATION("23503"),
    NULL_VALUE_NOT_ALLOWED("23502"),
    DATATYPE_MISMATCH("42804"),
    SYNTAX_ERROR("42601"),
    INSUFFICIENT_PRIVILEGE("42501");


}
private val sqlStateMap = PostgresErrorCodes.values().associateBy(PostgresErrorCodes::sqlState)
fun String.toErrorCode(): PostgresErrorCodes? {
    return sqlStateMap[this]
}

