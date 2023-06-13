package me.snitchon.example

import life.shank.ShankModule
import life.shank.single
import me.snitchon.example.ApplicationModule.clock

object UsersRepositoryModule : ShankModule {
    val usersRepository = single { -> PostgresUsersRepository() }
    val postsRepository = single { -> PostsRepository() }
}
