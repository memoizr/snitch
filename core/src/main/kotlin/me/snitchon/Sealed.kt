package me.snitchon

abstract class Sealed {
    val `$type`: String = this::class.simpleName!!
}
