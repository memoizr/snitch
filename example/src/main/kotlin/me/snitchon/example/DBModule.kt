package me.snitchon.example

import life.shank.ShankModule
import life.shank.single
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
    val db = single { -> DB(connection()) }
}