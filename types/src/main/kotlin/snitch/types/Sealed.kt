package snitch.types

abstract class Sealed {
    val `$type`: String = this::class.simpleName!!
}


