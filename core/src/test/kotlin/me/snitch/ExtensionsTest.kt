package me.snitch

import me.snitch.dsl.InlineSnitchTest
import me.snitch.service.Endpoint
import org.junit.Test

class ExtensionsTest: InlineSnitchTest() {
    @Test
    fun `allows for versioning`() {
        val baseVersion = "v1"

        infix fun <T: Any> Endpoint<T>.v(version: Int) = copy(
            path = path.replace("/$baseVersion/", "/v$version/")
        )

        given {
            baseVersion / {
                GET("home") isHandledBy { "this is v1".ok.plainText }
                GET("home") v 2 isHandledBy { "this is v2".ok.plainText }
            }
        } then {
            GET("/v1/home").expectCode(200).expectBody("this is v1")
            GET("/v2/home").expectCode(200).expectBody("this is v2")
        }
    }
}