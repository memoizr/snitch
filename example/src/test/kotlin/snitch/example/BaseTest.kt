package snitch.example

import org.junit.jupiter.api.TestInstance
import snitch.example.types.Email
import snitch.kofix.customize
import snitch.tests.SnitchTest
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest : SnitchTest({ Application.setup(it) }) {
    init {
        customize<Email> { Email("${randomString()}@${randomString()}.com") }
    }

    fun randomString(n: Int = 1, m: Int = 5): String {
        val length = Random.nextInt(n, m + 1) // to include m in the possible length
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(length) { chars.random() }.joinToString("")
    }
}