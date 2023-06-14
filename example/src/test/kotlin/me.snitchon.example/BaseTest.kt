package me.snitchon.example

import me.snitchon.example.database.DBModule
import me.snitchon.example.database.DBModule.connection
import me.snitchon.example.types.Email
import me.snitchon.tests.SnitchTest
import org.junit.jupiter.api.*
import ro.kreator.customize
import kotlin.random.Random

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest : SnitchTest({ Application.start(it) }) {
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