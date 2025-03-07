package snitch.example.database

import snitch.shank.ShankModule
import snitch.shank.single
import snitch.example.database.repositories.posts.PostgresPostsRepository
import snitch.example.database.repositories.posts.PostsRepository
import snitch.example.database.repositories.users.PostgresUsersRepository
import snitch.example.database.repositories.users.UsersRepository

object RepositoriesModule : ShankModule {
    val usersRepository = single<UsersRepository> { -> PostgresUsersRepository() }
    val postsRepository = single<PostsRepository> { -> PostgresPostsRepository() }
}
