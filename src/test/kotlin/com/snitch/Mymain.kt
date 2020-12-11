package com.snitch

import com.snitch.spark.SparkSnitchService

fun main() {
    SparkSnitchService(Config())
        .setRoutes {
            GET("fool")
                .isHandledBy {
                    "this is good".ok
                }
        }
}