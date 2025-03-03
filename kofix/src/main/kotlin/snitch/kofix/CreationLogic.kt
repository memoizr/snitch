package snitch.kofix

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import io.github.classgraph.ScanResult
import javassist.util.proxy.ProxyFactory
import javassist.util.proxy.ProxyObject
import org.apache.commons.lang3.RandomStringUtils
import java.io.File
import java.lang.reflect.Array.*
import java.lang.reflect.Method
import java.lang.reflect.TypeVariable
import java.security.MessageDigest
import java.time.Duration.ofDays
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.Collections.*
import kotlin.collections.set
import kotlin.reflect.*
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.internal.ReflectProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.javaMethod
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure
import kotlin.reflect.jvm.jvmName


typealias Token = Long

internal object CreationLogic {
    init {
        Seed.seed
        fun list(type: KType, kProperty: KProperty<*>?, token: Token, past: Set<KClass<*>>) =
            aList(type.arguments.first().type!!, token, past.plus(type.jvmErasure), kProperty)

        fun <T : Any> list(klass: KClass<T>, kProperty: KProperty<*>?, token: Token, past: Set<KClass<*>>): List<T> =
            aList(klass.createType(), token, past, kProperty) as List<T>

        fun map(type: KType, kProperty: KProperty<*>?, token: Token, past: Set<KClass<*>>) =
            list(type, kProperty, token, past)
                .map { Pair(it, instantiateRandomClass(type.arguments[1].type!!, kProperty, token)) }.toMap()

        val o = ObjectFactory

        o[typeOf<String>()] = { _, _, _, token -> aString(token) }
        o[typeOf<Byte>()] = { _, _, _, token -> aByte(token) }
        o[typeOf<Int>()] = { _, _, _, token -> anInt(token) }
        o[typeOf<Long>()] = { _, _, _, token -> aLong(token) }
        o[typeOf<Double>()] = { _, _, _, token -> aDouble(token) }
        o[typeOf<Short>()] = { _, _, _, token -> aShort(token) }
        o[typeOf<Float>()] = { _, _, _, token -> aFloat(token) }
        o[typeOf<Boolean>()] = { _, _, _, token -> aBoolean(token) }
        o[typeOf<Char>()] = { _, _, _, token -> aChar(token) }
        o[typeOf<IntArray>()] =
            { _, past, kproperty, token -> list(Int::class, kproperty, token, past).toIntArray() }
        o[typeOf<Array<Int>>()] =
            { _, past, kproperty, token -> list(Int::class, kproperty, token, past).toTypedArray() }
        o[typeOf<ShortArray>()] =
            { _, past, kproperty, token -> list(Short::class, kproperty, token, past).toShortArray() }
        o[typeOf<Array<Short>>()] =
            { _, past, kproperty, token -> list(Short::class, kproperty, token, past).toTypedArray() }
        o[typeOf<LongArray>()] =
            { _, past, kproperty, token -> list(Long::class, kproperty, token, past).toLongArray() }
        o[typeOf<Array<Long>>()] =
            { _, past, kproperty, token -> list(Long::class, kproperty, token, past).toTypedArray() }
        o[typeOf<FloatArray>()] =
            { _, past, kproperty, token -> list(Float::class, kproperty, token, past).toFloatArray() }
        o[typeOf<Array<Float>>()] =
            { _, past, kproperty, token -> list(Float::class, kproperty, token, past).toTypedArray() }
        o[typeOf<DoubleArray>()] =
            { _, past, kproperty, token -> list(Double::class, kproperty, token, past).toDoubleArray() }
        o[typeOf<Array<Double>>()] =
            { _, past, kproperty, token -> list(Double::class, kproperty, token, past).toTypedArray() }
        o[typeOf<BooleanArray>()] =
            { _, past, kproperty, token -> list(Boolean::class, kproperty, token, past).toBooleanArray() }
        o[typeOf<Array<Boolean>>()] =
            { _, past, kproperty, token -> list(Boolean::class, kproperty, token, past).toTypedArray() }
        o[typeOf<ByteArray>()] =
            { _, past, kproperty, token -> list(Byte::class, kproperty, token, past).toByteArray() }
        o[typeOf<Array<Byte>>()] =
            { _, past, kproperty, token -> list(Byte::class, kproperty, token, past).toTypedArray() }
        o[typeOf<CharArray>()] =
            { _, past, kproperty, token -> list(Char::class, kproperty, token, past).toCharArray() }
        o[typeOf<Array<Char>>()] =
            { _, past, kproperty, token -> list(Char::class, kproperty, token, past).toTypedArray() }

        o[typeOf<List<*>>()] = { type, past, kproperty, token -> list(type, kproperty, token, past) }
        o[typeOf<Set<*>>()] = { type, past, kproperty, token -> list(type, kproperty, token, past).toSet() }
        o[typeOf<Map<*, *>>()] =
            { type, past, kproperty, token -> map(type, kproperty, token, past) }

        o[typeOf<File>()] = { _, _, kproperty, token -> File(aString(token)) }
        o[typeOf<Date>()] = { _, _, kproperty, token -> Date(aLong(token)) }
        o[typeOf<Instant>()] = { _, _, kproperty, token ->
            Instant.ofEpochMilli(Seed.seed)
                .plusNanos(aLong(token, -ofDays(100).toNanos(), ofDays(100).toNanos()))
                .truncatedTo(ChronoUnit.MICROS)
        }
        o[typeOf<UUID>()] = { _, _, kproperty, token -> UUID.randomUUID() }
    }

    internal object ObjectFactory {
        private val objectFactories = mutableMapOf<KType, (KType, Set<KClass<*>>, KProperty<*>?, Token) -> Any>()

        operator fun set(type: KType, factory: (KType, Set<KClass<*>>, KProperty<*>?, Token) -> Any): ObjectFactory {
            objectFactories[type] = factory
            return this
        }

        operator fun get(type: KType): ((KType, Set<KClass<*>>, KProperty<*>?, Token) -> Any)? {
            return objectFactories[type] ?: objectFactories[type.jvmErasure.starProjectedType]
        }

        operator fun contains(type: KType): Boolean {
            return get(type)?.let { true } ?: false
        }
    }

    private val maxChar = 59319
    private val maxStringLength = 10

    private fun aChar(token: Long): Char = pseudoRandom(token).nextInt(maxChar).toChar()
    private fun anInt(token: Long, max: Int? = null): Int =
        max?.let { pseudoRandom(token).nextInt(it) } ?: pseudoRandom(token).nextInt()

    private fun aLong(token: Long): Long = pseudoRandom(token).nextLong()
    private fun aLong(token: Long, min: Long, max: Long): Long = pseudoRandom(token).nextLong(min, max)
    private fun aDouble(token: Long): Double = pseudoRandom(token).nextDouble()
    private fun aShort(token: Long): Short = pseudoRandom(token).nextInt(Short.MAX_VALUE.toInt()).toShort()
    private fun aFloat(token: Long): Float = pseudoRandom(token).nextFloat()
    private fun aByte(token: Long): Byte = pseudoRandom(token).nextInt(255).toByte()
    private fun aBoolean(token: Long): Boolean = pseudoRandom(token).nextBoolean()
    private fun aString(token: Long): String = pseudoRandom(token).let {
        RandomStringUtils.random(Math.max(3, it.nextInt(maxStringLength)), 0, maxChar, true, true, null, it)
    }

    private val md = MessageDigest.getInstance("MD5")

    val Any.hash: Long
        get() {
            val array = md.digest(toString().toByteArray())

            var hash = 7L
            for (i in array) {
                hash = hash * 31 + i.toLong()
            }
            return hash
        }

    internal infix fun Long.with(other: Long): Long {
        return this * 31 + other
    }

    internal fun aList(
        type: KType,
        token: Long, parentClasses: Set<KClass<*>>,
        kProperty: KProperty<*>?,
        size: Int? = null,
        minSize: Int = 1,
        maxSize: Int = 5
    ): List<*> {
        val klass = type.jvmErasure

        parentClasses.shouldNotContain(klass)

        val items = 0..(size ?: (pseudoRandom(token).nextInt(maxSize - minSize) + minSize))

        return items.map {
            if (klass == List::class) {
                aList(type.arguments.first().type!!, token.hash with it.hash, parentClasses, kProperty)
            } else instantiateRandomClass(type, token.hash with it.hash, parentClasses, kProperty)
        }
    }

    internal fun instantiateRandomClass(
        type: KType,
        token: Token = 0,
        parentClasses: Set<KClass<*>> = emptySet(),
        kProperty: KProperty<*>?
    ): Any? {
        val klass = type.jvmErasure
        parentClasses.shouldNotContain(klass)

        fun KClass<out Any>.isAnInterfaceOrSealed() = this.java.isInterface || this.isSealed || this.isAbstract
        fun KClass<out Any>.isAnArray() = this.java.isArray
        fun KClass<out Any>.isAnEnum() = this.java.isEnum
        fun KClass<out Any>.isAnObject() = this.objectInstance != null
        fun thereIsACustomFactory() = type in ObjectFactory
        fun isNullable(): Boolean = type.isMarkedNullable && pseudoRandom(token).nextInt() % 2 == 0

        return when {
            isNullable() -> null
            thereIsACustomFactory() -> ObjectFactory[type]?.invoke(type, parentClasses, kProperty, token)
            klass.isAnObject() -> klass.objectInstance
            klass.isAnEnum() -> klass.java.enumConstants[anInt(token, max = klass.java.enumConstants.size)]
            klass.isAnArray() -> instantiateArray(type, token, parentClasses, klass, kProperty)
            klass.isAnInterfaceOrSealed() -> instantiateAbstract(type, token, parentClasses, kProperty)
            else -> instantiateArbitraryClass(klass, token, type, parentClasses, kProperty)
        }
    }

    private fun instantiateArray(
        type: KType,
        token: Token,
        past: Set<KClass<*>>,
        klass: KClass<out Any>,
        kProperty: KProperty<*>?
    ): Array<Any?> {
        val genericType = type.arguments.first().type!!
        val list = aList(genericType, token, past.plus(klass), kProperty)
        val array = newInstance(genericType.jvmErasure.java, list.size) as Array<Any?>
        return array.apply { list.forEachIndexed { index, any -> array[index] = any } }
    }

    private fun instantiateAbstract(type: KType, token: Token, past: Set<KClass<*>>, kProperty: KProperty<*>?): Any {
        val klass = type.jvmErasure

        val allImplementationsInModule =
            if (klass.java.isInterface) subtypes.implementations[klass.java.name]
            else subtypes.subclasses[klass.java.name]

        return allImplementationsInModule
            ?.toList()?.map { it.loadClass() }
            ?.getOrNull(pseudoRandom(token).int(allImplementationsInModule.size))
            ?.let {
                val params = it.kotlin.typeParameters.map { KTypeProjection(it.variance, it.starProjectedType) }
                instantiateRandomClass(it.kotlin.createType(params), kProperty, token.hash with it.name.hash)
            }
            ?: instantiateNewInterface(type, token, past, kProperty)
    }

    private fun instantiateNewInterface(
        type: KType,
        token: Token,
        past: Set<KClass<*>>,
        kProperty: KProperty<*>?
    ): Any {

        val klass = type.jvmErasure
        val genericTypeNameToConcreteTypeMap = klass.typeParameters.map { it.name }.zip(type.arguments).toMap()

        fun degenerify(kType: KType): KType {
            return if (kType.arguments.isEmpty()) genericTypeNameToConcreteTypeMap[kType.javaType.typeName]?.type
                ?: kType
            else {
                val argumentField =
                    kType::class.java.declaredFields.find { it.name == "${kType::arguments.name}\$delegate" }!!
                argumentField.isAccessible = true
                val degenerifiedArguments = kType.arguments.map { KTypeProjection(it.variance, degenerify(it.type!!)) }
                val newFieldValue = ReflectProperties.lazySoft { degenerifiedArguments }
                argumentField.set(kType, newFieldValue)
                kType
            }
        }

        val javaMethods: Array<Method> = klass.java.methods + Any::class.java.methods

        val methodReturnTypes = javaMethods.map { method ->
            val returnType = klass.members.find { member ->
                fun hasNameName(): Boolean =
                    (method.name == member.name || method.name == "get${member.name.capitalize()}")

                fun hasSameArguments() =
                    method.parameterTypes.map { it.name } == member.valueParameters.map { it.type.jvmErasure.jvmName }
                hasNameName() && hasSameArguments()
            }?.returnType?.let { degenerify(it) }

            val type1 = genericTypeNameToConcreteTypeMap[returnType?.jvmErasure?.simpleName]?.type
            method to (type1 ?: returnType)
        }.toMap()

        val factory = ProxyFactory()

        if (klass.java.isInterface) {
            factory.interfaces = arrayOf(klass.java)
        } else {
            factory.superclass = klass.java
        }

        val proxy = factory.createClass().constructors.filter { !it.isSynthetic && it.parameterCount == 0 }.first()
            .newInstance()

        (proxy as ProxyObject).setHandler { proxxy, method, _, obj ->
            when (method.name) {
                Any::hashCode.javaMethod?.name -> proxxy.toString().hashCode()
                Any::equals.javaMethod?.name -> proxxy.toString() == obj[0].toString()
                Any::toString.javaMethod?.name -> "\$RandomImplementation$${klass.simpleName}"
                else -> methodReturnTypes[method]?.let { instantiateRandomClass(it, token, past, kProperty) }
                    ?: instantiateRandomClass(method.returnType.kotlin.createType(), token, past, kProperty)
            }
        }
        return proxy
    }

    private fun Set<KClass<*>>.shouldNotContain(klass: KClass<*>) {
        if (isAllowedCyclic(klass) && this.contains(klass)) throw CyclicException()
    }

    private fun instantiateArbitraryClass(
        klass: KClass<out Any>,
        token: Token,
        type: KType,
        past: Set<KClass<*>>,
        kProperty: KProperty<*>?
    ): Any? {
        val constructors = klass.constructors.filter { !it.parameters.any { (it.type.jvmErasure == klass) } }.toList()
        if (constructors.isEmpty() && klass.constructors.any { it.parameters.any { (it.type.jvmErasure == klass) } }) throw CyclicException()
        val defaultConstructor = constructors[pseudoRandom(token).int(constructors.size)] as KFunction<*>
        if (!defaultConstructor.isAccessible) {
            defaultConstructor.isAccessible = true
        }
        val constructorTypeParameters by lazy {
            defaultConstructor.valueParameters.map {
                it.type.toString().replace("!", "").replace("?", "")
            }.toMutableList()
        }
        val typeMap by lazy { type.jvmErasure.typeParameters.map { it.name }.zip(type.arguments).toMap() }
        val pairedConstructor = defaultConstructor.parameters.map {
            if (it.type.javaType is TypeVariable<*>) constructorTypeParameters.get(it.index) to it else "" to it
        }
        val parameters = (pairedConstructor.map { (first, second: KParameter) ->
            fun isTypeVariable() = second.type.javaType is TypeVariable<*>
            val tpe = if (isTypeVariable()) typeMap[first]?.type ?: second.type else second.type
            instantiateRandomClass(
                tpe,
                token.hash with tpe.jvmErasure.simpleName!!.hash with second.name!!.hash,
                past.plus(klass),
                kProperty
            )
        }).toTypedArray()
        try {
            val res = defaultConstructor.call(*parameters)
            return res
        } catch (e: Throwable) {
            val namedParameters =
                parameters.zip(defaultConstructor.parameters.map { it.name }).map { "${it.second}=${it.first}" }
            throw CreationException(
                """Something went wrong when trying to instantiate class ${klass}
         using constructor: $defaultConstructor
         with values: $namedParameters""", e.cause
            )
        }
    }

    private fun Random.int(bound: Int) = if (bound == 0) 0 else nextInt(bound)
    private fun isAllowedCyclic(klass: KClass<out Any>) =
        klass != List::class && klass != Set::class && klass != Map::class && !klass.java.isArray

    private fun pseudoRandom(token: Long): Random = Random(Seed.seed with token)

    val subtypes by lazy {
        val subclasses = mutableMapOf<String, MutableSet<ClassInfo>>()
        val implementations = mutableMapOf<String, MutableSet<ClassInfo>>()
        scanResult
            .allClasses
            .parallelStream()
            .forEach { klass ->
                klass.superclasses
                    .forEach {
                        subclasses.get(it.name)?.add(klass) ?: subclasses.put(it.name, mutableSetOf(klass))
                    }
                klass.interfaces
                    .forEach {
                        implementations.get(it.name)?.add(klass) ?: implementations.put(
                            it.name,
                            mutableSetOf(klass)
                        )
                    }
            }
        SubTypes(subclasses, implementations)
    }


    private val scanResult: ScanResult by lazy { ClassGraph().enableAllInfo().scan() }

    data class SubTypes(
        val subclasses: MutableMap<String, MutableSet<ClassInfo>>,
        val implementations: MutableMap<String, MutableSet<ClassInfo>>
    )
}
