package snitch.exposed

import org.jetbrains.exposed.sql.transactions.transaction
import snitch.router.decorateWith

val withTransaction get() = decorateWith { transaction { next() } }
