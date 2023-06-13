package me.snitchon.example

import life.shank.ShankModule
import life.shank.new
import life.shank.single
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit

object ApplicationModule : ShankModule {
    val clock = single { -> Clock.systemUTC() }
    val now = new { -> Instant.now(clock()).truncatedTo(ChronoUnit.MILLIS) }
}
