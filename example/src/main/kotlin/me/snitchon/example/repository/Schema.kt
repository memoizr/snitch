package me.snitchon.example.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

val dbSchema = listOf(Users, Posts).toTypedArray()

object Users : Table() {
    val id = text("id").uniqueIndex()
    val name = text("name")
    val email = text("email").uniqueIndex()
    val hash = text("hash")
    override val primaryKey = PrimaryKey(id)
}

object Posts: Table() {
    val id = text("id").uniqueIndex()
    val title = text("title")
    val content = text("content")
    val creatorId = text("creator_id").references(Users.id).index()
    val createdAt = timestamp("created_at").index()

    override val primaryKey = PrimaryKey(id)
}
