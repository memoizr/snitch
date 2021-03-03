plugins {
    kotlin("jvm")
    maven
}

group = "com.snitch.spark"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation(kotlin("stdlib"))
    implementation("com.sparkjava:spark-core:2.9.3")
    implementation("ch.qos.logback:logback-classic:1.1.7")
}
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
