package me.snitchon.types

abstract class Sealed {
    val `$type`: String = this::class.simpleName!!
}
