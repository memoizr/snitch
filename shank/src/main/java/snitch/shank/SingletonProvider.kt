package snitch.shank

class SingleProvider0<T>(
    val factory: () -> T
) : Provider0<T> {
    private var cache: T? = null
    private var o: (() -> T)? = null
    fun override(f: (() -> T)?) = also { o = f }.also { cache = null }

    @Synchronized
    override fun invoke(): T = cache ?: (o?.invoke() ?: factory()).also { cache = it }
}

class SingleProvider1<A, T>(
    val factory: (A) -> T
) : Provider1<A, T>, SingleProvider1Interface<A, T> {
    private var cache: HashcodeHashMap<T>? = null
    private var o: ((A) -> T)? = null
    override fun override(f: ((A) -> T)?) = also { o = f }.also { cache = null }

    @Synchronized
    override fun invoke(a: A): T = cache?.get(this or a)
        ?: (o?.invoke(a) ?: factory(a)).also {
            (cache ?: apply { cache = HashcodeHashMap() }.cache)
                ?.put(this or a, it)
        }
}

class SingleProvider2<A, B, T>(
    val factory: (A, B) -> T
) : Provider2<A, B, T>, SingleProvider2Interface<A, B, T> {
    private var cache: HashcodeHashMap<T>? = null
    private var o: ((A, B) -> T)? = null
    override fun override(f: ((A, B) -> T)?) = also { o = f }.also { cache = null }

    @Synchronized
    override fun invoke(a: A, b: B): T = cache?.get(this or a or b)
        ?: (o?.invoke(a, b) ?: factory(a, b)).also {
            (cache ?: apply { cache = HashcodeHashMap() }.cache)
                ?.put(this or a or b, it)
        }
}

class SingleProvider3<A, B, C, T>(
    val factory: (A, B, C) -> T
) : Provider3<A, B, C, T>, SingleProvider3Interface<A, B, C, T> {
    private var cache: HashcodeHashMap<T>? = null
    private var o: ((A, B, C) -> T)? = null
    override fun override(f: ((A, B, C) -> T)?) = also { o = f }.also { cache = null }

    @Synchronized
    override fun invoke(a: A, b: B, c: C): T = cache?.get(this or a or b or c)
        ?: (o?.invoke(a, b, c) ?: factory(a, b, c)).also {
            (cache ?: apply { cache = HashcodeHashMap() }.cache)
                ?.put(this or a or b or c, it)
        }
}

class SingleProvider4<A, B, C, D, T>(
    val factory: (A, B, C, D) -> T
) : Provider4<A, B, C, D, T>, SingleProvider4Interface<A, B, C, D, T> {
    private var cache: HashcodeHashMap<T>? = null
    private var o: ((A, B, C, D) -> T)? = null
    override fun override(f: ((A, B, C, D) -> T)?) = also { o = f }.also { cache = null }

    @Synchronized
    override fun invoke(a: A, b: B, c: C, d: D): T = cache?.get(this or a or b or c or d)
        ?: (o?.invoke(a, b, c, d) ?: factory(a, b, c, d)).also {
            (cache ?: apply { cache = HashcodeHashMap() }.cache)
                ?.put(this or a or b or c or d, it)
        }
}

class SingleProvider5<A, B, C, D, E, T>(
    val factory: (A, B, C, D, E) -> T
) : Provider5<A, B, C, D, E, T>, SingleProvider5Interface<A, B, C, D, E, T> {
    private var cache: HashcodeHashMap<T>? = null
    private var o: ((A, B, C, D, E) -> T)? = null
    override fun override(f: ((A, B, C, D, E) -> T)?) = also { o = f }.also { cache = null }

    @Synchronized
    override fun invoke(a: A, b: B, c: C, d: D, e: E): T = cache?.get(this or a or b or c or d or e)
        ?: (o?.invoke(a, b, c, d, e) ?: factory(a, b, c, d, e)).also {
            (cache ?: apply { cache = HashcodeHashMap() }.cache)
                ?.put(this or a or b or c or d or e, it)
        }
}

// Renamed the interfaces to avoid naming conflicts with the implementation classes
interface SingleProvider1Interface<A, T> : Provider1<A, T> {
    infix fun override(f: ((A) -> T)?): SingleProvider1Interface<A, T>
}

interface SingleProvider2Interface<A, B, T> : Provider2<A, B, T> {
    infix fun override(f: ((A, B) -> T)?): SingleProvider2Interface<A, B, T>
}

interface SingleProvider3Interface<A, B, C, T> : Provider3<A, B, C, T> {
    infix fun override(f: ((A, B, C) -> T)?): SingleProvider3Interface<A, B, C, T>
}

interface SingleProvider4Interface<A, B, C, D, T> : Provider4<A, B, C, D, T> {
    infix fun override(f: ((A, B, C, D) -> T)?): SingleProvider4Interface<A, B, C, D, T>
}

interface SingleProvider5Interface<A, B, C, D, E, T> : Provider5<A, B, C, D, E, T> {
    infix fun override(f: ((A, B, C, D, E) -> T)?): SingleProvider5Interface<A, B, C, D, E, T>
}

inline fun <T> ShankModule.single(noinline factory: () -> T) = SingleProvider0<T>(factory)

inline fun <A, T> ShankModule.single(noinline factory: (A) -> T) = SingleProvider1<A, T>(factory)

inline fun <A, B, T> ShankModule.single(noinline factory: (A, B) -> T) = SingleProvider2<A, B, T>(factory)

inline fun <A, B, C, T> ShankModule.single(noinline factory: (A, B, C) -> T) = SingleProvider3<A, B, C, T>(factory)

inline fun <A, B, C, D, T> ShankModule.single(noinline factory: (A, B, C, D) -> T) = SingleProvider4<A, B, C, D, T>(factory)

inline fun <A, B, C, D, E, T> ShankModule.single(noinline factory: (A, B, C, D, E) -> T) = SingleProvider5<A, B, C, D, E, T>(factory)

