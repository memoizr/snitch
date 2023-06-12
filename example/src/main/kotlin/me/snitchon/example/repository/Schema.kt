package me.snitchon.example.repository

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = text("id").uniqueIndex()
    val name = text("name")
    val email = text("email").uniqueIndex()
    val hash = text("hash")
    override val primaryKey = PrimaryKey(id)
}
