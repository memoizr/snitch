package com.snitch

import com.snitch.spark.SparkSnitchService
import org.junit.Rule
import org.junit.Test
import spark.Service.ignite
import java.security.Provider

class YoTest : SparkTest() {
//    @Rule
//    @JvmField
//    val rule: SparkTestRule = SparkTestRule(port) {
//    }


    @Test
    fun yo() {
        SparkSnitchService(Config())
            .setRoutes(ServerRouter)
            .startListening()
    }
}