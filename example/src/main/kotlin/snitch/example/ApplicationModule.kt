package snitch.example

import snitch.shank.ShankModule
import snitch.shank.new
import snitch.shank.single
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Instant
import java.time.temporal.ChronoUnit


object ApplicationModule : ShankModule {
    val clock = single { -> Clock.systemUTC() }
    val now = new { -> Instant.now(clock()).truncatedTo(ChronoUnit.MILLIS) }
    val logger = single { -> ExampleLoggerImpl(LoggerFactory.getLogger("app")) }
}
