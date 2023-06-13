package me.snitchon.example.database

import life.shank.ShankModule
import life.shank.single
import me.snitchon.example.PostgresDatabase
import org.jetbrains.exposed.sql.Database

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