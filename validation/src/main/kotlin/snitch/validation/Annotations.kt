package snitch.validation

@Target(AnnotationTarget.PROPERTY)
annotation class StringLength(val min: Int = 0, val max: Int = Int.MAX_VALUE)

@Target(AnnotationTarget.PROPERTY)
annotation class RegexPattern(val pattern: String)

@Target(AnnotationTarget.PROPERTY)
annotation class Length(val min: Int = 0, val max: Int = Int.MAX_VALUE)

@Target(AnnotationTarget.PROPERTY)
annotation class Min(val value: Double)

@Target(AnnotationTarget.PROPERTY)
annotation class Max(val value: Double)