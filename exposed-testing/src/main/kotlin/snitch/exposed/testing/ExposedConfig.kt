package snitch.exposed.testing

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table

data class ExposedConfig(
    val database: Database,
    val tables: List<Table>
)