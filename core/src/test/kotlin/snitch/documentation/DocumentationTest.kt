package snitch.documentation

import com.memoizr.assertk.expect
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import snitch.dsl.InlineSnitchTest
import snitch.parameters.header
import snitch.parameters.optionalHeader
import snitch.parameters.optionalQuery
import snitch.parameters.path
import snitch.parameters.query
import snitch.types.Sealed
import java.util.*

class DocumentationTest : InlineSnitchTest() {
    @Test
    fun `creates documentation to basic endpoint`() {
        val doc = given {
            GET("/foo").isHandledBy { "bar".ok }
            PUT("/foo").isHandledBy { "bar".ok }
            POST("/foo").isHandledBy { "bar".ok }
            PATCH("/foo").isHandledBy { "bar".ok }
            DELETE("/foo").isHandledBy { "bar".ok }
        }.generateDocumentation()
            .documentation

        expect that doc.spec contains "get" contains "/foo"
    }

    @Test
    fun `deals with complex classes`() {
        expect that given {
            GET() isHandledBy { (null as ComplexClass).ok }
        }.generateDocumentation()
            .documentation
            .spec contains ""
    }

    @Test
    @Disabled
    fun `deals with parameter types`() {
        val path by path()
        val header by header()
        val optionalHeader by optionalHeader()
        val query by query()
        val optionalQuery by optionalQuery()
        expect that given {
            GET(path)
                .with(headers(header, optionalHeader))
                .with(queries(query, optionalQuery))
                .isHandledBy { (null as ComplexClass).ok }
        }
            .generateDocumentation()
            .documentation
            .spec contains "ohmaria" contains "aliasdescription"
    }

    @Test
    fun `serves the public documentation`() {
        given {
            GET("/foo").isHandledBy { "bar".ok }
        }
            .generateDocumentation()
            .servePublicDocumenation()
    }
}

@Description("aliasdescription")
typealias MyBoolean = Boolean
private data class ComplexClass(
    @Description("description", exInt = 1)
    val int: Int,
    @Description("description", exLong = 2)
    val long: Long,
    @Description("description", exFloat = 2f)
    val float: Float,
    @Description("description", exDouble = 2.0)
    val double: Double,
    @Description("description", exString = "ohmaria")
    val string: String,
    val boolean: MyBoolean,
    val short: Short,
    val byte: Byte,
    val char: Char,
    val list: List<String>,
    val set: Set<String>,
    val map: Map<String, String>,
    val array: ByteArray,
    val sealed: SealedClass,
    val date: Date,
    val myenum: MyEnum,
    @Description("description", exInt = 1)
    val maybeint: Int?,
    @Description("description", exLong = 2)
    val maybelong: Long?,
    @Description("description", exFloat = 2f)
    val maybefloat: Float?,
    @Description("description", exDouble = 2.0)
    val maybedouble: Double?,
    @Description("description", exString = "")
    val maybestring: String?,
    val maybeboolean: Boolean?,
    val maybeshort: Short?,
    val maybebyte: Byte?,
    val maybechar: Char?,
    val maybelist: List<String>?,
    val maybeset: Set<String>?,
    val maybemap: Map<String, String>?,
    val maybearray: ByteArray?,
    val maybesealed: SealedClass?,
    val maybedate: Date?,
    val maybemyenum: MyEnum?,
)

private enum class MyEnum {
    A, B
}

private sealed class SealedClass : Sealed() {
    object A : SealedClass()
    object B : SealedClass()
}