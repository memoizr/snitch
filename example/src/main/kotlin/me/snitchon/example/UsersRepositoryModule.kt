package me.snitchon.example

import life.shank.ShankModule
import life.shank.single

object UsersRepositoryModule : ShankModule {
    val usersRepository = single { -> PostgresUsersRepository(DBModule.connection()) }
}