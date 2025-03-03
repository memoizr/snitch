package snitch.exposed

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnSet
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionAlias
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.update
import snitch.exposed.AutoMapper.mapping
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties

object AutoMapper {
    val mapping = mutableMapOf<KClass<*>, Mapper<Any, Any>>()

    inline fun <reified FROM, TO> customMapping(noinline from: (FROM) -> TO, noinline to: (TO) -> FROM) {
        mapping[FROM::class] = Mapper(from, to) as Mapper<Any, Any>
    }
}

data class Mapper<FROM, TO>(val from: (FROM) -> TO, val to: (TO) -> FROM)

inline fun <reified R : Any> Any.to(): R = this.to(R::class)
fun <R : Any> Any.to(target: KClass<R>): R {
    if (this::class == target) return this as R
    if (target.isValue) return mapValueClass(target, this) as R

    val members = this::class.members.toList().map { it.name to it }.toMap()

    val constructor = target.constructors.first()
    constructor.parameters
        .map { it.name to it }.toMap()

    try {
        val args = constructor.parameters.map {
            val sourceValue = try {
                members[it.name]?.call(this)?.unwrap()
            } catch (e: Exception) {
                println("Error getting value for ${it.name} from $this")
                throw e
            }
            val targetClass = it.type.classifier as KClass<*>

            it to if (sourceValue == null) {
                null
            } else {
                if (sourceValue is Set<*>) {
                    val setClass = it.type.arguments.first().type!!.classifier as KClass<*>
                    sourceValue.map { it?.to(setClass) }.toSet()
                } else if (sourceValue is List<*>) {
                    val setClass = it.type.arguments.first().type!!.classifier as KClass<*>
                    sourceValue.map { it?.to(setClass) }.toList()
                } else if (targetClass.isValue) {
                    mapValueClass(targetClass, sourceValue)
                } else if (targetClass != sourceValue::class) {
                    try {
                        mapping[sourceValue::class]?.from?.invoke(sourceValue)
                            ?: mapping[targetClass]?.to?.invoke(sourceValue)
                            ?: sourceValue.to(targetClass)
                    } catch (e: Exception) {
                        println("Error mapping $sourceValue to $targetClass")
                        throw e
                    }
                } else sourceValue
            }
        }
        val instance = constructor.callBy(
            args
                .toMap()
                .filterNot { (!it.key.type.isMarkedNullable && it.value == null) }
        )
        return instance

    } catch (e: Exception) {
        println("Error mapping $this to ${target}")
        throw e
    }
}

private fun mapValueClass(targetClass: KClass<*>, sourceValue: Any) = try {
    val first = targetClass.constructors.first()
    val kClass = first.parameters.first().type.classifier as KClass<*>
    if (kClass != sourceValue::class) {
        (
                mapping[sourceValue::class]?.from?.invoke(sourceValue)
                    ?: mapping[kClass]?.to?.invoke(sourceValue)
                )
            ?.wrap(targetClass)
    } else {
        first.call(sourceValue)
    }
} catch (e: Exception) {
    println("Error instantiating value $sourceValue to $targetClass")
    throw e
}

fun Any.unwrap() = if (this::class.isValue) this::class.members.first().call(this) else this
fun Any.wrap(target: KClass<*>) = if (target.isValue) target.constructors.first().call(this) else this

inline fun <reified R : Any> Table.to(result: ResultRow): R = result.to(this, R::class)
fun <T : ColumnSet, R : Any> ResultRow.to(from: T, to: KClass<R>): R {
    val namedColumns =
        from.columns
            .map { it.table }
            .flatMap { table ->
                table::class.memberProperties
                    .map { property ->
                        property.name to if ((property.returnType.classifier as KClass<*>).isSubclassOf(Column::class)) {
                            property.call(table::class.objectInstance) as? Expression<R>
                        } else {
                            null
                        }
                    }
                    .filter { (_, column) -> column != null }
            }
            .toMap()


    val constructor = to.constructors.first()
    val params = constructor.parameters.map {
        it to namedColumns[it.name]
            ?.let { expr ->
                val message = this@to.get(expr)
                if (message is ExposedBlob) message.bytes
                else message
            }
            ?.wrap(it.type.classifier as KClass<*>)
    }
        .filter { (_, column) -> column != null }
        .toMap()

    return constructor.callBy(params)
}

fun <R : Any> Table.insert(value: R, customize: Table.(UpdateBuilder<*>) -> Unit = {}): InsertStatement<Number> =
    this.insert {
        customize(it)
        autoInsert(it, value)
    }

fun <R : Any> Table.batchInsert(values: List<R>, customize: Table.(UpdateBuilder<*>) -> Unit = {}): List<ResultRow> =
    this.batchInsert(values) {
        customize(this)
        autoInsert(this, it)
    }

inline fun <reified R : Any> InsertStatement<Number>.id() =
    this.table.primaryKey?.columns?.find { it.name == "id" }?.let { this[it]?.wrap(R::class) } as R

inline fun <reified R : Any> Table.findAll(e: SqlExpressionBuilder.() -> Op<Boolean> = { Op.TRUE }) = select(e)
    .map { it.to(this, R::class) }

inline fun <reified R : Any> Table.findOne(e: SqlExpressionBuilder.() -> Op<Boolean>) = select(e)
    .map { it.to(this, R::class) }
    .single()

inline fun <reified R : Any> Table.findOneOrNull(e: SqlExpressionBuilder.() -> Op<Boolean>) = select(e)
    .map { it.to(this, R::class) }
    .singleOrNull()

inline fun <reified R : Any> Table.updateWhere(value: R, noinline e: SqlExpressionBuilder.() -> Op<Boolean>) =
    this.update(e) {
        autoInsert(it, value)
    }

fun <R : Any> Table.autoInsert(statement: UpdateBuilder<*>, value: R) {
    val namedColumns = this::class
        .memberProperties
        .map { property ->
            property.name to if ((property.returnType.classifier as KClass<*>).isSubclassOf(Column::class)) {
                property.call(this) as? Column<Any?>
            } else {
                null
            }
        }
        .filter { (_, column) -> column != null }
        .toMap()

    val namedValues = value::class
        .memberProperties
        .map { property ->
            property.name to property.call(value)?.unwrap()
        }
        .toMap()

    namedColumns.forEach { (name, column) ->
        namedValues[name]?.let { statement[column!!] = it }
    }
}

fun Expression<*>.toField(property: KProperty<*>): ExpressionAlias<*> = ExpressionAlias(this, property.name)

val columnNameToPropertyName = mutableMapOf<String, String>()

operator fun <T> Column<T>.getValue(table: Table, property: KProperty<*>): Column<T> {
    columnNameToPropertyName[this.name] = property.name
    return this
}