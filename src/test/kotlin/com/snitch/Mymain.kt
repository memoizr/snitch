package com.snitch

import com.google.gson.Gson
import com.snitch.spark.JoobySnitchService
import io.jooby.Jooby
import io.jooby.runApp
import io.ktor.utils.io.core.*
import kotlin.text.toByteArray

fun main() {
    JoobySnitchService(Config())
        .setRoutes {
            GET("fool")
                .isHandledBy {
                    "this is good".ok
                }
        }

    "".toByteArray()
//    Gson().fromJson()


}

class JoobyApp: Jooby() {

}