package snitch.exposed.testing

import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

interface ExposedTest {
    val exposedConfig: ExposedConfig

    @BeforeEach
    fun beforeEach() {
        transaction(exposedConfig.database) {
            SchemaUtils.create(*exposedConfig.tables.toTypedArray())
        }
    }

    @AfterEach
    fun afterEach() {
        transaction(exposedConfig.database) {
            SchemaUtils.drop(*exposedConfig.tables.toTypedArray())
        }
    }
}
