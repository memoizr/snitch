package snitch.example.database

import life.shank.ShankModule
import life.shank.single
import org.jetbrains.exposed.sql.Database
import snitch.example.PostgresDatabase

object DBModule : ShankModule {
    val connection = single { ->
        Database.connect(
            "jdbc:postgresql://localhost:5432/postgres",
            driver = "org.postgresql.Driver",
            user = "postgres",
            password = "postgres"
        )
    }
    val postgresDatabase = single { -> PostgresDatabase(connection()) }
}