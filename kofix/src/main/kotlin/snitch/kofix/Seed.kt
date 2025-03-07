package snitch.kofix

object Seed {
    internal var testing = false

    var seed = System.currentTimeMillis()
        set(value) {
            field = value
            if (!testing) println("Random-Object-Kreator - Overriding seed: $value")
        }

    init {
        println("Random-Object-Kreator - Setting seed (system-time): $seed")
    }
}