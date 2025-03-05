package snitch.exposed

import org.jetbrains.exposed.sql.Database
import snitch.shank.ShankModule
import snitch.shank.single

object ExposedModule : ShankModule {
    val connection = single { config: DatabaseConnectionConfig ->
        Database.connect(
            url = config.url,
            driver = config.driver,
            user = config.user,
            password = config.password,
            setupConnection = config.setupConnection,
            databaseConfig = config.databaseConfig,
            manager = config.manager
        )
    }
    val postgresDatabase = single { config: DatabaseConnectionConfig ->
        ExposedDatabase(connection(config))
    }
}
