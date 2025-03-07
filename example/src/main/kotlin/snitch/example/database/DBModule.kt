package snitch.example.database

import snitch.exposed.postgres.postgresConnectionConfig
import snitch.shank.ShankModule
import snitch.shank.single

object DBModule : ShankModule {
    val connectionConfig = single { -> postgresConnectionConfig() }
    val schema = single { -> dbSchema.asList() }
}