package me.snitchon.example.database

import life.shank.ShankModule
import life.shank.single
import me.snitchon.example.database.repositories.users.PostgresUsersRepository
import me.snitchon.example.database.repositories.posts.PostgresPostsRepository
import me.snitchon.example.database.repositories.posts.PostsRepository
import me.snitchon.example.database.repositories.users.UsersRepository

object RepositoriesModule : ShankModule {
    val usersRepository = single<UsersRepository> { -> PostgresUsersRepository() }
    val postsRepository = single<PostsRepository> { -> PostgresPostsRepository() }
}
