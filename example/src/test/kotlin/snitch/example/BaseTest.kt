package snitch.example

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import ro.kreator.customize
import snitch.example.database.DBModule
import snitch.example.database.DBModule.connection
import snitch.example.types.Email
import snitch.tests.SnitchTest
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest : SnitchTest({ Application.setup(it) }) {
    init {
        connection()
        customize<Email> { Email("${randomString()}@${randomString()}.com") }
    }

    fun randomString(n: Int = 1, m: Int = 5): String {
        val length = Random.nextInt(n, m + 1) // to include m in the possible length
        val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return List(length) { chars.random() }.joinToString("")
    }

    @BeforeAll
    fun beforeAll() {
        super.before()
    }

    @AfterAll
    fun afterAll() {
        super.after()
    }

    @BeforeEach
    override fun before() {
        DBModule.postgresDatabase().createSchema()
    }

    @AfterEach
    override fun after() {
        DBModule.postgresDatabase().dropSchema()
    }
}