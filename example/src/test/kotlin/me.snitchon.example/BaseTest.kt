package me.snitchon.example

import me.snitchon.example.database.DBModule
import me.snitchon.example.database.DBModule.connection
import me.snitchon.tests.SnitchTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class BaseTest : SnitchTest({ Application.start(it) }) {
    init {
        connection()
    }

    @BeforeEach
    override fun before() {
        DBModule.postgresDatabase().createSchema()
        activeService.service.onStop { DBModule.postgresDatabase().dropSchema() }
        super.before()
    }

    @AfterEach
    override fun after() {
        super.after()
    }
}