package snitch.exposed

import life.shank.ShankModule
import life.shank.single
import org.jetbrains.exposed.sql.Database

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
