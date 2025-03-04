package snitch.shank

class NewProvider0<T>(
    val factory: () -> T
) : Provider<T, () -> T> {
    private var o: (() -> T)? = null
    infix fun override(f: (() -> T)?): NewProvider0<T> = apply { o = f }
    operator fun invoke(): T = o?.invoke() ?: factory()
}

class NewProvider1<A, T>(
    val factory: (A) -> T
) : Provider<T, (A) -> T> {
    private var o: ((A) -> T)? = null
    infix fun override(f: ((A) -> T)?): NewProvider1<A, T> = apply { o = f }
    operator fun invoke(a: A): T = o?.invoke(a) ?: factory(a)
}

class NewProvider2<A, B, T>(
    val factory: (A, B) -> T
) : Provider<T, (A, B) -> T> {
    private var o: ((A, B) -> T)? = null
    infix fun override(f: ((A, B) -> T)?): NewProvider2<A, B, T> = apply { o = f }
    operator fun invoke(a: A, b: B): T = o?.invoke(a, b) ?: factory(a, b)
}

class NewProvider3<A, B, C, T>(
    val factory: (A, B, C) -> T
) : Provider<T, (A, B, C) -> T> {
    private var o: ((A, B, C) -> T)? = null
    infix fun override(f: ((A, B, C) -> T)?): NewProvider3<A, B, C, T> = apply { o = f }
    operator fun invoke(a: A, b: B, c: C): T = o?.invoke(a, b, c) ?: factory(a, b, c)
}

class NewProvider4<A, B, C, D, T>(
    val factory: (A, B, C, D) -> T
) : Provider<T, (A, B, C, D) -> T> {
    private var o: ((A, B, C, D) -> T)? = null
    infix fun override(f: ((A, B, C, D) -> T)?): NewProvider4<A, B, C, D, T> = apply { o = f }
    operator fun invoke(a: A, b: B, c: C, d: D): T = o?.invoke(a, b, c, d) ?: factory(a, b, c, d)
}

class NewProvider5<A, B, C, D, E, T>(
    val factory: (A, B, C, D, E) -> T
) : Provider<T, (A, B, C, D, E) -> T> {
    private var o: ((A, B, C, D, E) -> T)? = null
    infix fun override(f: ((A, B, C, D, E) -> T)?): NewProvider5<A, B, C, D, E, T> = apply { o = f }
    operator fun invoke(a: A, b: B, c: C, d: D, e: E): T = o?.invoke(a, b, c, d, e) ?: factory(a, b, c, d, e)
}

inline fun <T> ShankModule.new(noinline factory: () -> T) = NewProvider0<T>(factory)

inline fun <A, T> ShankModule.new(noinline factory: (A) -> T) = NewProvider1<A, T>(factory)

inline fun <A, B, T> ShankModule.new(noinline factory: (A, B) -> T) = NewProvider2<A, B, T>(factory)

inline fun <A, B, C, T> ShankModule.new(noinline factory: (A, B, C) -> T) = NewProvider3<A, B, C, T>(factory)

inline fun <A, B, C, D, T> ShankModule.new(noinline factory: (A, B, C, D) -> T) = NewProvider4<A, B, C, D, T>(factory)

inline fun <A, B, C, D, E, T> ShankModule.new(noinline factory: (A, B, C, D, E) -> T) = NewProvider5<A, B, C, D, E, T>(factory)
